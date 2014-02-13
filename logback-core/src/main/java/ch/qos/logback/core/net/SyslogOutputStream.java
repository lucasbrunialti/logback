/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * SyslogOutputStream is a wrapper around the {@link DatagramSocket} class so
 * that it behaves like an {@link OutputStream}.
 */
public class SyslogOutputStream extends OutputStream {

	private InetAddress address;
	private ByteArrayOutputStream baos = new ByteArrayOutputStream();
	final private int port;

	private Socket socket;

	public SyslogOutputStream(String syslogHost, int port)
			throws UnknownHostException, SocketException {
		this.address = InetAddress.getByName(syslogHost);
		this.port = port;

		try {
			this.socket = new Socket(address, port);
		} catch (IOException e) {
			throw new SocketException();
		}
	}

	public void write(byte[] byteArray, int offset, int len) throws IOException {
		baos.write(byteArray, offset, len);
	}

	public void flush() throws IOException {
		byte[] bytes = baos.toByteArray();

		// after a failure, it can happen that bytes.length is zero
		// in that case, there is no point in sending out an empty message/
		if (bytes.length == 0) {
			return;
		}

		if (socket == null || socket.isClosed() || socket.isOutputShutdown()
				|| !socket.isConnected()) {
			System.out.println("Lost connection.");
			throw new IOException();
		}

		// clean up for next round
		baos = new ByteArrayOutputStream();
		baos.reset();

		try {
			socket.getOutputStream().write(bytes);
			socket.getOutputStream().flush();
		} catch (SocketException e) {
			System.out.println("Caught socket exception while writing to socket: "
							+ e);
			throw new IOException(e);
		} catch (IOException e) {
			System.out.println("Caught io exception while writing to socket: "
					+ e);
			throw e;
		}

	}

	public void close() throws IOException {
		address = null;
		if (socket != null)
			socket.close();
	}

	public int getPort() {
		return port;
	}

	@Override
	public void write(int b) throws IOException {
		baos.write(b);
	}

	int getSendBufferSize() throws SocketException {
		return socket.getSendBufferSize();
	}
}
