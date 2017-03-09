package com.dc.nettyserver.proto;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Scanner;

/**
 * 结合protoBuf实现自己代码生成器
 * @author gavin
 *
 */
public class ClassGenerator {
	public static void mains(String[] args) {
		/**
		 * 这个命令执行的路径根据自己protoBuf的安装路劲配置
		 */
		String strCmd = "D:/protobuf/protobuf-master/protoc.exe -I=D:/eclipse-nettyclient/rpc/nettyserver/src/main/java/com/gc/nettyserver/proto --java_out=D:/eclipse-nettyclient/rpc/nettyserver/src/main/java/com/gc/nettyserver/pojo D:/eclipse-nettyclient/rpc/nettyserver/src/main/java/com/gc/nettyserver/proto/conf.proto";
		try {
			InputStream is = Runtime.getRuntime().exec(strCmd).getInputStream();
			byte[] buffer = new byte[1024];
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while (is.read(buffer) > -1) {
				baos.write(buffer);
			}
			System.out.println(new String(baos.toByteArray()));
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * procol.proc 协议配置文件，第一个方法名，第二个messageid，第三个protoBuf对象名称，第四个对应客户端messageid
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	private void writeHandler() throws IOException {
		// 扫描配置
		String path = System.getProperty("user.dir");
		String procolPath = path + "/src/main/java/com/gc/nettyserver/proto/";
		String messageIdPath = path + "/src/main/java/com/gc/nettyserver/pojo/";
		String handlerPath = path + "/src/main/java/com/gc/nettyserver/pojo/";
		// 打开messageId
		File messageIdFile = new File(messageIdPath + "MessageId.java");
		FileChannel messageIdFC = new FileOutputStream(messageIdFile).getChannel();
		messageIdFC.write(ByteBuffer.wrap("package com.gc.nettyserver.pojo;".getBytes()));
		messageIdFC.write(ByteBuffer.wrap("\n".getBytes()));
		messageIdFC.write(ByteBuffer.wrap("public class MessageId {".getBytes()));
		messageIdFC.write(ByteBuffer.wrap("\n".getBytes()));
		messageIdFC.write(ByteBuffer.wrap("public static final int HEATBEAT= 0;//默认心跳".getBytes("utf-8")));
		messageIdFC.write(ByteBuffer.wrap("\n".getBytes()));
		// 打开SingleHandler
		File singleHandlerFile = new File(handlerPath + "SingleHandler.java");
		FileChannel singleHandlerFC = new FileOutputStream(singleHandlerFile).getChannel();
		singleHandlerFC.write(ByteBuffer.wrap("package com.gc.nettyserver.pojo;".getBytes()));
		singleHandlerFC.write(ByteBuffer.wrap("\n".getBytes()));
		singleHandlerFC.write(ByteBuffer.wrap("import com.gc.rpc.connection.Connection;".getBytes()));
		singleHandlerFC.write(ByteBuffer.wrap("\n".getBytes()));
		singleHandlerFC.write(ByteBuffer.wrap("import com.gc.rpc.connection.ConnectionManager;".getBytes()));
		singleHandlerFC.write(ByteBuffer.wrap("\n".getBytes()));
		singleHandlerFC.write(ByteBuffer.wrap("import com.gc.rpc.connection.ConnectionState;".getBytes()));
		singleHandlerFC.write(ByteBuffer.wrap("\n".getBytes()));
		singleHandlerFC.write(ByteBuffer.wrap("import com.google.protobuf.InvalidProtocolBufferException;".getBytes()));
		singleHandlerFC.write(ByteBuffer.wrap("\n".getBytes()));
		singleHandlerFC.write(ByteBuffer.wrap("public abstract class SingleHandler {".getBytes()));
		singleHandlerFC.write(ByteBuffer.wrap("\n".getBytes()));
		Scanner scaner = new Scanner(new File(procolPath + "procol.proc"));
		StringBuffer sb = new StringBuffer();// handler process函数
		sb.append("public void process(com.gc.rpc.packages.Package pack,Connection connection){\n");
		sb.append("MessageResponse.Response.Builder builder=MessageResponse.Response.newBuilder();\n");
		sb.append("com.gc.rpc.packages.Package p=null;\n");
		sb.append(
				"if(pack.getMessageId()!=MessageId.CLIENT_TO_SERVER_REGISTER&&!connection.getState().equals(ConnectionState.Login)){\n");
		sb.append("if(connection.getState().equals(ConnectionState.Closed)){\n");
		sb.append("ConnectionManager.getInstance().remove(connection.getChannel());\n");
		sb.append("return;\n");
		sb.append("}\n");
		sb.append("builder.setRes(0);\n");
		sb.append("p=new com.gc.rpc.packages.Package(MessageId.SERVER_TO_CLIENT_REGISTER,builder.build());\n");
		sb.append("connection.send(p, null);\n");
		sb.append("return;\n");
		sb.append("}\n");
		sb.append("switch (pack.getMessageId()) {\n");
		sb.append("case MessageId.HEATBEAT:\n");
		sb.append("builder.setRes(1);\n");
		sb.append("p=new com.gc.rpc.packages.Package(MessageId.HEATBEAT,builder.build());\n");
		sb.append("connection.send(p, null);\n");
		sb.append("return;\n");
		while (scaner.hasNextLine()) {
			String str = scaner.nextLine();
			String[] strs = str.split(":");
			// 写messageid
			messageIdFC.write(ByteBuffer
					.wrap(("public static final int SERVER_TO_CLIENT_" + strs[0].toUpperCase() + "=" + strs[1] + ";")
							.getBytes("utf-8")));
			messageIdFC.write(ByteBuffer.wrap("\n".getBytes()));
			messageIdFC.write(ByteBuffer
					.wrap(("public static final int CLIENT_TO_SERVER_" + strs[0].toUpperCase() + "=" + strs[3] + ";")
							.getBytes("utf-8")));
			messageIdFC.write(ByteBuffer.wrap("\n".getBytes()));
			// 写handler
			singleHandlerFC.write(ByteBuffer.wrap(("public abstract void " + strs[0] + "(Connection connection, Message"
					+ strs[2] + "." + strs[2] + " proto);").getBytes()));
			singleHandlerFC.write(ByteBuffer.wrap("\n".getBytes()));
			// proccess 函数
			sb.append("case MessageId.SERVER_TO_CLIENT_" + strs[0].toUpperCase() + ":\n");
			sb.append("Message" + strs[2] + "." + strs[2] + " proto" + strs[1] + ";\n");
			sb.append("try {\n");
			sb.append("proto" + strs[1] + " = Message" + strs[2] + "." + strs[2] + ".parseFrom(pack.getBody());\n");
			sb.append(strs[0] + "(connection,proto" + strs[1] + ");\n");
			sb.append("} catch (InvalidProtocolBufferException e) {\n");
			sb.append("e.printStackTrace();\n");
			sb.append("}\n");
			sb.append("return;\n");
		}
		messageIdFC.write(ByteBuffer.wrap("}".getBytes()));
		messageIdFC.close();
		sb.append("default:\n");
		sb.append("break;\n");
		sb.append("}\n");
		sb.append("}\n");
		sb.append("}");
		singleHandlerFC.write(ByteBuffer.wrap(sb.toString().getBytes()));
		singleHandlerFC.close();

	}

	public static void main(String[] msg) throws IOException {
		new ClassGenerator().writeHandler();
	}

}
