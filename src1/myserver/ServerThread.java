package myserver;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Set;

import myserver.configs.Configs;

//服务器端读取客户端发送来的消息的子线程
public class ServerThread extends Thread {

	/*
	 * private InputStream in;
	 * 
	 * public ServerThread(InputStream in) { this.in = in; }
	 */

	private Socket s;
	// private ArrayList<Socket> list ;
	private HashMap<String, Socket> hm;
	private String username;

	public ServerThread(Socket s, HashMap<String, Socket> hm, String username) {
		this.s = s;
		this.hm = hm;
		this.username = username;

	}

	@Override
	public void run() {

		try {
			// 获取客户端所在通道内的输入和输出流
			OutputStream out = s.getOutputStream();
			InputStream in = s.getInputStream();

			// 不断读取
			while (true) {
				byte[] bys = new byte[1024 * 10];
				int len = in.read(bys);

				// msgStr它是客户端发送来的消息
				// 之前的消息格式:接收者:消息内容:发送者

				// 现在拿到客户端发送过来消息: 接收者:消息内容:消息类型
				// 现在的消息格式
				String msgStr = new String(bys, 0, len).trim(); // 去掉两端空格
				System.out.println(msgStr);

				// 拆分消息
				String[] msgs = msgStr.split(":");
				// 接收者
				String receiver = msgs[0];
				// 消息内容
				String msgContent = msgs[1];
				// 消息类型
				// String msgType = msgs[2] ;
				int msgType = Integer.parseInt(msgs[2]);

				// 拆分消息之后,重新组装消息,约定服务器转发格式
				// 发送者:消息内容:消息类型:时间
				long time = System.currentTimeMillis();

				// 服务器端读取消息的子线程应该根据不同的消息类型做出不同的处理
				if (msgType == Configs.MSG_PRIVATE) {
					// 私聊处理

					// 要符合转发格式
					// 获取接收者所在的通道内的Socket对象
					Socket socket = hm.get(receiver);  // 接收者的Socket对象
					// 通过socket对象获取当前接收者的通道 内的输出流
					// 转发的消息格式:
					// 发送者:消息内容:消息类型:时间
					String zfMsg = username + ":" + msgContent + ":" + msgType + ":" + time;
					//getOutputStream():返回Socket对象的输出流对象
					socket.getOutputStream().write(zfMsg.getBytes());
				} else if (msgType == Configs.MSG_PUBLIC) {
					// 公聊处理
					// 遍历集合
					Set<String> keySet = hm.keySet();
					for (String key : keySet) {
						// 排除自己
						if (key.equals(username)) {
							continue;// 立即进入下一次循环
						}

						// 需要给出了自己的所有人发送
						// 获取当前用户名所在的通道的内的Socket对象
						Socket socket = hm.get(key);
						// 获取通道当前用户的通道内的输出流对象
						OutputStream os = socket.getOutputStream();

						// 符合转发格式:组装消息: 发送者:消息内容:消息类型:系统时间
						String zfMsg = username + ":" + msgContent + ":" + Configs.MSG_PUBLIC + ":" + time;

						os.write(zfMsg.getBytes());
					}
				} else if (msgType == Configs.MSG_ONLIST) {
					// 在线列表
					// 创建一个字符串缓冲区对象
					StringBuffer sb = new StringBuffer();

					int i = 1;
					// 逻辑:遍历HashMap集合 取出键值 输出 获取每一个用户,排除自己
					Set<String> keySet = hm.keySet();
					for (String key : keySet) {
						// 排除自己
						if (key.equals(username)) {
							continue;
						}

						// 需要给除了自己的人发送,"我"上线了
						// 需要容器
						// 拼接
						sb.append((i++)).append(",").append(key).append("\n");

						// 获取谁所在的通道的Socket对象 获取发送者的Socket对象,将在线用户发送给发送者

						// 组装消息,转发:格式:发送者:消息内容:消息类型:时间
						String zfMsg = username + ":" + sb.toString() + ":" + Configs.MSG_ONLIST + ":" + time;
						// 要获取除了自己的所有用户,所以要用到集合中的健值关系
						hm.get(username).getOutputStream().write(zfMsg.getBytes());
					}
				} else if (msgType == Configs.MSG_EXIT) {
					// 下线处理

					// 遍历集合,排除自己
					Set<String> keySet = hm.keySet();
					for (String key : keySet) {
						// 排除自己
						if (key.equals(username)) {
							continue;
						}

						// 获取Socket对象
						Socket socket = hm.get(key);
						// 组装下线
						String zfMsg = username + ":" + "下线了" + ":" + Configs.MSG_EXIT + ":" + time;
						socket.getOutputStream().write(zfMsg.getBytes());
					}

					break;
				} else if (msgType == Configs.MSG_FILE) {
					// 发送文件
					// 将文件名称和文件大小拆分出来
					String[] fileInfo = msgContent.split("#");
					String fileName = fileInfo[0];
					long fileLength = Long.parseLong(fileInfo[1]);

					// 组装消息:发送者:消息内容:消息类型:时间
					String msg = username + ":" + msgContent + ":" + Configs.MSG_FILE + ":" + time;

					// 获取msg的消息字节数组
					byte[] msgBytes = msg.getBytes();
					// 文件两端空格去掉了,中间也会有空字节
					byte[] emptyByte = new byte[1024 * 10 - msgBytes.length];

					// 读取文件
					// 定义一个缓冲区
					byte[] cacheBytes = new byte[1024];
					int cacheLength = 0; // 读取的实际的有效字节数
					// 使用客户端所在通道内的输入流去读这个文件,然后在使用内存操作流,将读到的内容存储到内存操作流中
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					while (true) {
						int len2 = in.read(cacheBytes);
						bos.write(cacheBytes, 0, len2);
						cacheLength += len2;// 每次记录读到的实际有效字节数

						if (cacheLength == fileLength) {// 一旦读取完毕,停掉
							break;
						}
					}

					// //要么重置一下流对象,要么重新创建一个流的对象
					byte[] fileByes = bos.toByteArray(); // 获取文件字节数组
					bos.reset();

					// 获取到文件的所在的字节数组
					bos.write(msgBytes);
					bos.write(emptyByte);
					bos.write(fileByes);

					// 在拼成一个大的字节数组
					byte[] allBytes = bos.toByteArray();

					// 转发
					out.write(allBytes);

					// 转发出去
				}

				// 之前的格式
				// 接收者msgs[0].发送者

				// 获取接收者所在的通的内流
				// 任何包装类类型都有对应的parseXXX()方法 ---> Long.parseLong(long) ; Double Byte
				// Socket socket = list.get(Integer.parseInt(msgs[0]));
				// socket.getOutputStream().write((msgs[2]+"对你说"+msgs[1]).getBytes());

			}

			// username关闭掉自己的所在Socket对象.并且从集合中将自己移出掉
			hm.get(username).close(); // 关闭会SocketException,做空处理
			hm.remove(username);
		} catch (SocketException e) {
			//空处理
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
