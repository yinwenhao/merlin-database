package com.magic.synchronize.merkletree;

import java.util.ArrayList;
import java.util.List;

import com.magic.util.CRC32;

public class MerkleTreeNode extends MerkleTreeNodeBase {

	private int level; // 层级，根节点为0

	private MerkleTreeNodeBase leftSon;

	private MerkleTreeNodeBase rightSon;

	public MerkleTreeNode(int keyCrc32, int level) {
		super();
		this.level = level;
		setKeyCrc32(keyCrc32);
	}

	/**
	 * 获得node下所有的叶子
	 * 
	 * @return
	 */
	public List<MerkleTreeLeaf> allLeafUnderNode() {
		List<MerkleTreeLeaf> result = new ArrayList<MerkleTreeLeaf>();
		// 后序遍历
		if (this.getLeftSon() != null) {
			if (this.getLeftSon() instanceof MerkleTreeNode) {
				result.addAll(((MerkleTreeNode) this.getLeftSon()).allLeafUnderNode());
			} else {
				result.add((MerkleTreeLeaf) this.getLeftSon());
			}
		}
		if (this.getRightSon() != null) {
			if (this.getRightSon() instanceof MerkleTreeNode) {
				result.addAll(((MerkleTreeNode) this.getRightSon()).allLeafUnderNode());
			} else {
				result.add((MerkleTreeLeaf) this.getRightSon());
			}
		}
		return result;
	}

	/**
	 * 根据keyCrc32找到MerkleTreeNode
	 * 
	 * @param keyCrc32
	 * @return
	 */
	public MerkleTreeNodeBase getMerkleTreeNodeByKeyCrc32(int keyCrc32) {
		MerkleTreeNode result = this;
		MerkleTreeNodeBase r;
		while (result.getKeyCrc32() != keyCrc32) {
			if (keyCrc32 < result.getKeyCrc32()) {
				r = result.getLeftSon();
			} else {
				r = result.getRightSon();
			}
			if (r != null && r instanceof MerkleTreeNode) {
				result = (MerkleTreeNode) r;
			} else {
				// 这里没有对应的MerkleTreeNode，需要通知那边把这个keyCrc32的MerkleTreeNode下的所有leaf发送过来
				return r;
			}
		}
		return result;
	}

	/**
	 * 增加或更新一个叶子节点，不会更新hash值
	 * 
	 * @param leaf
	 */
	public void addOrUpdateLeafWithoutHash(String key, int hash) {
		addOrUpdateLeaf(key, hash, false);
	}

	/**
	 * 增加或更新一个叶子节点，会更新hash值
	 * 
	 * @param key
	 * @param hash
	 * @return true：重新计算了hash false：hash值没变
	 */
	public boolean addOrUpdateLeaf(String key, int hash) {
		return addOrUpdateLeaf(key, hash, true);
	}

	/**
	 * 
	 * @param key
	 * @param hash
	 * @param updateHash
	 * @return true：重新计算了hash false：hash值没变
	 */
	private boolean addOrUpdateLeaf(String key, int hash, boolean updateHash) {
		boolean result = false;
		CRC32 crc32 = new CRC32();
		crc32.update(key);
		int keyCrc32 = crc32.getValue();
		if (keyCrc32 < this.getKeyCrc32()) {
			// 在左边
			if (this.getLeftSon() == null) {
				this.setLeftSon(new MerkleTreeLeaf(hash, key));
				result = true;
			} else {
				// 需要判断是叶子节点还是非叶子节点
				if (this.getLeftSon() instanceof MerkleTreeLeaf) {
					// 是叶子节点，需要生成非叶子节点
					MerkleTreeLeaf oldLeftSon = (MerkleTreeLeaf) this.getLeftSon();
					if (oldLeftSon.getKeyCrc32() == keyCrc32) {
						result = result || oldLeftSon.checkAddKey(key, hash);
					} else {
						MerkleTreeNode left = new MerkleTreeNode(
								this.getKeyCrc32() - (int) Math.pow(2, 30 - this.getLevel()), this.getLevel() + 1);
						this.setLeftSon(left);

						if (oldLeftSon.getKeyCrc32() < left.getKeyCrc32()) {
							left.setLeftSon(oldLeftSon);
						} else {
							left.setRightSon(oldLeftSon);
						}
						result = result || ((MerkleTreeNode) this.getLeftSon()).addOrUpdateLeaf(key, hash, updateHash);
					}
				} else {
					result = result || ((MerkleTreeNode) this.getLeftSon()).addOrUpdateLeaf(key, hash, updateHash);
				}
			}
		} else {
			// 在右边
			if (this.getRightSon() == null) {
				this.setRightSon(new MerkleTreeLeaf(hash, key));
				result = true;
			} else {
				// 需要判断是叶子节点还是非叶子节点
				if (this.getRightSon() instanceof MerkleTreeLeaf) {
					// 是叶子节点，需要生成非叶子节点
					MerkleTreeLeaf oldRightSon = (MerkleTreeLeaf) this.getRightSon();
					if (oldRightSon.getKeyCrc32() == keyCrc32) {
						result = result || oldRightSon.checkAddKey(key, hash);
					} else {
						MerkleTreeNode right = new MerkleTreeNode(
								this.getKeyCrc32() + (int) Math.pow(2, 30 - this.getLevel()), this.getLevel() + 1);
						this.setRightSon(right);

						if (oldRightSon.getKeyCrc32() < right.getKeyCrc32()) {
							right.setLeftSon(oldRightSon);
						} else {
							right.setRightSon(oldRightSon);
						}
						result = result || ((MerkleTreeNode) this.getRightSon()).addOrUpdateLeaf(key, hash, updateHash);
					}
				} else {
					result = result || ((MerkleTreeNode) this.getRightSon()).addOrUpdateLeaf(key, hash, updateHash);
				}
			}
		}
		if (result && updateHash) {
			crc32.reset();
			if (this.getLeftSon() != null) {
				crc32.update(this.getLeftSon().getHash());
			}
			if (this.getRightSon() != null) {
				crc32.update(this.getRightSon().getHash());
			}
			setHash(crc32.getValue());
		}
		return result;
	}

	/**
	 * 填充每个节点的hash值
	 */
	public void fillHashForMerkleTree() {
		// 后序遍历，为每个MerkleTreeNode设置hash值
		CRC32 crc32 = new CRC32();
		if (this.getLeftSon() != null) {
			if (this.getLeftSon() instanceof MerkleTreeNode) {
				((MerkleTreeNode) this.getLeftSon()).fillHashForMerkleTree();
			}
			crc32.update(this.getLeftSon().getHash());
		}
		if (this.getRightSon() != null) {
			if (this.getRightSon() instanceof MerkleTreeNode) {
				((MerkleTreeNode) this.getRightSon()).fillHashForMerkleTree();
			}
			crc32.update(this.getRightSon().getHash());
		}
		this.setHash(crc32.getValue());
	}

	public MerkleTreeNodeBase getLeftSon() {
		return leftSon;
	}

	public void setLeftSon(MerkleTreeNodeBase leftSon) {
		this.leftSon = leftSon;
	}

	public MerkleTreeNodeBase getRightSon() {
		return rightSon;
	}

	public void setRightSon(MerkleTreeNodeBase rightSon) {
		this.rightSon = rightSon;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

}
