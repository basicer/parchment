package com.basicer.parchment.extra;

import com.basicer.parchment.*;
import com.basicer.parchment.parameters.*;
import com.basicer.parchment.tcl.OperationalTCLCommand;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by basicer on 2/11/14.
 */
public class Redis extends TCLCommand {



	@Override
	public String[] getArguments() { return new String[] { "args"}; }

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		List<Parameter> args =  ctx.getArgs();
		return new BranchEvaluationResult(null, null, new RedisGuy(args));
	}


	private class RedisGuy implements EvaluationResult.EvalCallback {
		int state = 0;
		SocketChannel client = null;
		private List<Parameter> list;
		public RedisGuy(List<Parameter> list) {
			this.list = list;
		}
		@Override
		public EvaluationResult result(EvaluationResult last) {
			try {
				switch ( state ) {
					case 0:
						client = SocketChannel.open();
						client.configureBlocking(false);
						client.connect(new InetSocketAddress("localhost", 6379));
						++state;
						break;
					case 1:
						if ( client.finishConnect() ) ++state;
						break;
					case 2:
						client.configureBlocking(true);
						ByteBuffer b = encode(String.format("*%d\r\n", list.size()));
						while ( b.hasRemaining() ) client.write(b);

						for ( Parameter p : list ) {
							String s = p.asString();
							b = encode(String.format("$%d\r\n%s\r\n", s.length(), s));
							while ( b.hasRemaining() ) client.write(b);
						}
						++state;
						break;
					case 3:
						return EvaluationResult.makeOkay(readRedisResult(client));
				}
				return new BranchEvaluationResult(null, null, this);
			}
			catch ( IOException ex ) {
				return EvaluationResult.makeError(ex.getMessage());
			}
		}
	}


	private static Parameter readRedisResult(final SocketChannel client) throws IOException {
		ByteBuffer type = ByteBuffer.allocate(1);
		client.read(type);
		type.flip();
		char rtype = (char)type.get();
		switch ( rtype ) {
			case '$':
				int length = readLengthAndCRLF(client);
				if ( length == -1 ) return null;
				ByteBuffer stringb = ByteBuffer.allocate(length);
				client.read(stringb);
				stringb.flip();
				Parameter result = Parameter.from(decoder.decode(stringb).toString());
				client.read(ByteBuffer.allocate(2)); //Read CRLF;
				return result;
			case '*':
				int count = readLengthAndCRLF(client);
				ArrayList<Parameter> out = new ArrayList<Parameter>(count);
				for ( int i = 0; i < count; ++i ) out.add(readRedisResult(client));
				return ListParameter.from(out);
			case ':':
				int value = readLengthAndCRLF(client);
				return Parameter.from(value);
			case '+':
				return Parameter.from(readStringUntilCRFL(client));
			case '-':
				throw new FizzleException(readStringUntilCRFL(client));
		}

		return null;
	}

	private static String readStringUntilCRFL(final SocketChannel client) throws IOException {
		StringBuilder b = new StringBuilder();
		ByteBuffer c = ByteBuffer.allocate(1);
		while ( true ) {
			client.read(c);
			c.flip();
			char cc = (char)c.get();
			c.flip();

			if ( cc == '\r' ) {
				client.read(c); //Read LF
				return b.toString();
			}

			b.append(cc);
		}

	}

	private static int readLengthAndCRLF(final SocketChannel client) throws IOException {
		int count = 0;
		ByteBuffer c = ByteBuffer.allocate(1);
		while ( true ) {
			client.read(c);
			c.flip();
			char cc = (char)c.get();
			c.flip();

			if ( cc == '\r' ) {
				client.read(c); //Read LF
				return count;
			}

			int n = cc - '0';
			if ( n > 9 || n < 0 ) throw new IOException("Invalid character here");
			count = count * 10 + n;


		}
	}

	private static Charset charset = Charset.forName("UTF-8");
	private static CharsetEncoder encoder = charset.newEncoder();
	private static CharsetDecoder decoder = charset.newDecoder();

	public static ByteBuffer encode(String msg){
		try{
			return encoder.encode(CharBuffer.wrap(msg));
		}catch(Exception ex){
			throw new FizzleException(ex.getMessage());
		}
	}




}
