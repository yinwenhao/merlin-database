package com.magic.service.domain;

public class MagicServiceInstance {

	private String address;

	private int port;

	public MagicServiceInstance() {
	}

	public MagicServiceInstance(String address, int port) {
		this.address = address;
		this.port = port;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof MagicServiceInstance) {
			MagicServiceInstance s = (MagicServiceInstance) o;
			if (port != s.getPort()) {
				return false;
			} else {
				if (address == null) {
					if (s.getAddress() == null) {
						return true;
					} else {
						return false;
					}
				} else {
					if (address.equals(s.getAddress())) {
						return true;
					} else {
						return false;
					}
				}
			}
		}
		return false;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
