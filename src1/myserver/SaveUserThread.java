package myserver;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Set;

import myserver.configs.Configs;

//服务器端开启单独的保存用户名线程
public class SaveUserThread extends Thread {

	private Socket s;
	private HashMap<String, Socket> hm;
	private String username;

	public SaveUserThread(Socket s, HashMap<String, Socket> hm) {
		this.s = s;
		this.hm = hm;
	}

	@Override
	public void run() {

		try {

			// 获取通道内输入输出流
			InputStream in = s.getInputStream();
			OutputStream out = s.getOutputStream();

			// 不断地保存用户名
			// 不断读取客户端传递过来的username
			while (true) {
				byte[] bys = new byte[1024];
				int len = in.read(bys);
				username = new String(bys, 0, len);
				System.out.println(username);

				// 判断用户名是否存在,并且服务器给客户端做出反馈
				// 如果当前集合中不存在这个用户名才该用户名和它对应的Socket添加到集合中
				// public boolean containsKey(Object key):如果此映射包含对于指定键的映射关系,则返回 true
				if (!hm.containsKey(username)) {// 集合中没有username,才添加

					// 存进来(username用户名和它对应的通道 Socket)
					hm.put(username, s);

					// 给客户端的反馈用户名的校验:自己约定是否保存成功
					// yes:表示成功 no 表示失败
					out.write("yes".getBytes());
					break;
				} else {
					// 给客户端的反馈用户名的校验
					out.write("no".getBytes());
				}
			}

			// 保存用户名之后--->上线提醒:遍历集合,获取每个用户名对应的通道的Socket对象,然后获取他们给自
			// 输出流对象,写给自己 ,当然,排除自己!
			Set<String> keySet = hm.keySet();
			// 遍历所有的注册成功的用户名
			for (String key : keySet) {

				// 如果排除自己
				if (key.equals(username)) {
					continue; // 立即进入下一次循环
				}

				Socket socket = hm.get(key);
				OutputStream os = socket.getOutputStream();

				// 保存用户名的线程:
				// 上线功能的发送的消息的内容要遵循服务器转发的消息格式

				// 遵循服务器转格式
				// 转发的消息格式:
				// 发送者:消息内容:消息类型:时间
				String zfMsg = username + ":" + "上线了" + ":" + Configs.MSG_ONLINE + ":" + System.currentTimeMillis();

				os.write(zfMsg.getBytes());

				// 之前的上线提醒的发送的格式
				// os.write((username+"上线了").getBytes());

			}

			// 开启聊天线程
			new ServerThread(s, hm, username).start();

			// 整个保存用户名和上线提醒完成了, 这里开启聊天线程
			// ServeThread:服务端的读取客户端消息的子线程

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
