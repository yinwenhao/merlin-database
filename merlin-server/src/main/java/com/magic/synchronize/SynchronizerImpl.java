package com.magic.synchronize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.magic.bitcask.core.BitCaskServer;
import com.magic.bitcask.entity.BitCaskValue;
import com.magic.constants.Constants;
import com.magic.netty.request.Request;
import com.magic.server.options.ServerOptions;
import com.magic.service.Register;
import com.magic.service.domain.MagicServiceInstance;
import com.magic.synchronize.merkletree.MerkleTreeLeaf;
import com.magic.synchronize.merkletree.MerkleTreeNode;
import com.magic.synchronize.merkletree.MerkleTreeNodeBase;
import com.magic.timer.ScheduledExecutorServiceTimer;
import com.magic.timer.Timer;
import com.magic.timer.TimerFuture;

/**
 * 同步，最终一致性
 * 
 * @author yinwenhao
 *
 */
public class SynchronizerImpl implements Synchronizer, Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());

	private Register register;

	private ServerOptions opts;

	private List<OneClient> clients = new ArrayList<OneClient>();

	private BitCaskServer bitcask;

	private Timer timer;

	private TimerFuture timerFuture;

	public SynchronizerImpl(Register register, ServerOptions opts, BitCaskServer bitcask) throws Exception {
		this.opts = opts;
		this.register = register;
		this.bitcask = bitcask;

		this.timer = ScheduledExecutorServiceTimer.getInstance();
	}

	public void listServer() throws Exception {
		Collection<MagicServiceInstance> instances = register.queryForInstances(opts.getServiceName());
		if (instances.size() > 0) {
			Map<String, MagicServiceInstance> map = new HashMap<String, MagicServiceInstance>(instances.size() - 1);
			for (MagicServiceInstance instance : instances) {
				if (!register.getSelfInstance().equals(instance)) {
					map.put(instance.getAddress() + instance.getPort(), instance);
				}
			}
			List<OneClient> cc = new ArrayList<OneClient>(instances.size());
			for (OneClient c : clients) {
				String key = c.getAddressAndPort();
				if (map.containsKey(key)) {
					map.remove(key);
					cc.add(c);
				} else {
					c.shutdown();
				}
			}
			for (Entry<String, MagicServiceInstance> en : map.entrySet()) {
				cc.add(new OneClient(en.getValue(), opts));
			}
			clients = cc;
		} else {
			log.info("no service instance for {}", opts.getServiceName());
			for (OneClient c : clients) {
				c.shutdown();
			}
			clients = new ArrayList<OneClient>();
		}
	}

	@Override
	public void set(String key, String value, long version, long expire) throws Exception {
		log.debug("set key:{} value:{} version:{} expire:{}", key, value, version, expire);
		bitcask.innerSetWithExpire(key, value, version, expire);
	}

	@Override
	public void needValue(String keyCrc32) throws Exception {
		MerkleTreeNodeBase node = bitcask.getMerkleTreeRoot().getMerkleTreeNodeByKeyCrc32(Integer.valueOf(keyCrc32));
		if (node instanceof MerkleTreeLeaf) {
			doSetRequest((MerkleTreeLeaf) node);
		} else {
			List<MerkleTreeLeaf> leafs = ((MerkleTreeNode) node).allLeafUnderNode();
			for (MerkleTreeLeaf leaf : leafs) {
				doSetRequest(leaf);
			}
		}
	}

	@Override
	public void checkHash(String keyCrc32, String hashString) throws Exception {
		int keyCrc32Int = Integer.valueOf(keyCrc32);
		MerkleTreeNodeBase node = bitcask.getMerkleTreeRoot().getMerkleTreeNodeByKeyCrc32(keyCrc32Int);
		if (node == null) {
			// 发送信号，请求对方发送keyCrc32下所有的值
			requestForValue(keyCrc32);
			return;
		}
		if (node.getHash() == Integer.valueOf(hashString)) {
			// 一样的，不需要继续同步
			return;
		}
		if (node instanceof MerkleTreeLeaf) {
			if (node.getKeyCrc32() == keyCrc32Int) {
				// 发送这个key的值，用于同步
				doSetRequest((MerkleTreeLeaf) node);
			} else {
				// 发送信号，请求对方发送keyCrc32下所有的值
				requestForValue(keyCrc32);
			}
			return;
		} else {
			MerkleTreeNode n = (MerkleTreeNode) node;
			if (n.getLeftSon() == null && n.getRightSon() == null) {
				// 发送信号，请求对方发送keyCrc32下所有的值
				requestForValue(keyCrc32);
				return;
			}
			if (n.getLeftSon() != null) {
				doSynchronize(n.getLeftSon());
			}
			if (n.getRightSon() != null) {
				doSynchronize(n.getRightSon());
			}
		}
	}

	private void requestForValue(String keyCrc32) {
		sendRequest(Constants.GOSSIP_REQUIRE, keyCrc32, "", 0);
	}

	private void doSetRequest(MerkleTreeLeaf leaf) throws IOException {
		for (String key : leaf.getKeys()) {
			setRequest(bitcask.getRealValue(key));
		}
	}

	private void setRequest(BitCaskValue bcv) {
		sendRequest(new Request(Constants.GOSSIP_SET, bcv.getKey(), bcv.getValue(), bcv.getVersion(), bcv.getExpire()));
	}

	private void doSynchronize(MerkleTreeNodeBase node) {
		sendRequest(Constants.GOSSIP, String.valueOf(node.getKeyCrc32()), String.valueOf(node.getHash()), 0);
	}

	private void sendRequest(String method, String key, String value, long version) {
		Request request = new Request(method, key, value, version, 0);
		sendRequest(request);
	}

	private void sendRequest(Request request) {
		for (OneClient oc : clients) {
			try {
				oc.send(request);
			} catch (Exception e) {
				log.error("Synchronizer send error", e);
			}
		}
	}

	@Override
	public void start() throws Exception {
		timer.scheduleAtFixedRate(this, 0, opts.periodMilliSeconds, TimeUnit.MILLISECONDS);
	}

	@Override
	public void run() {
		try {
			listServer();
		} catch (Exception e) {
			log.error("listServer run error.", e);
		}
		try {
			MerkleTreeNode merkleTreeNode = bitcask.getMerkleTreeRoot();
			doSynchronize(merkleTreeNode);
		} catch (Exception e) {
			log.error("Synchronizer run error.", e);
		}
	}

	@Override
	public void shutdown() throws Exception {
		if (timerFuture != null) {
			if (!timerFuture.cancel(false)) {
				log.warn("timerFuture.cancel(false) failed");
				if (!timerFuture.cancel(true)) {
					log.error("timerFuture.cancel(true) failed");
				}
			}
		}
	}

}
