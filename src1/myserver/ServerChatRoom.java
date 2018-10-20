package myserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * 需求:客户端不断发送消息和读取服务器端反馈的消息,服务器端不断读取消息并反馈
 *
 */
public class ServerChatRoom {

	public static void main(String[] args) {
		try {
			// 创建服务器端的Socket对象
			ServerSocket ss = new ServerSocket(8888);
			// 创建一个单例集合,来存储客户端所在的Socket对象
			// ArrayList<Socket> list = new ArrayList<Socket>() ;

			//用来存放用户名和它所对应的Socket对象
			HashMap<String, Socket> hm = new HashMap<String, Socket>();
			System.out.println("服务器已开启,正在等待客户端的连接...");

			// 定一个变量用来记录用户端的个数
			int i = 1;
			// 使用while循环,表示只要有用户就需要不停的检测用户名,
			while (true) {
				// 监听客户端连接
				Socket s = ss.accept();
				System.out.println("第" + (i++) + "个客户端已经连接了...");

				// 将客户端添加到集合中
				// list.add(s) ;//角标从0开始,添加客户端

				// 获取服务端所在通道内的输入和输出流
				/*
				 * InputStream in = s.getInputStream() ; OutputStream out = s.getOutputStream()
				 * ;
				 */

				// 服务器端要先检验客户端输入的用户名,然后才能开启聊天线程
				SaveUserThread st = new SaveUserThread(s, hm);
				st.start();

				// 聊天线程:服务器读消息的子线程
				// ServerThread st = new ServerThread(s,hm) ;
				// st.start();

			}

			// 创建键盘录入对象,回复消息
			// Scanner sc = new Scanner(System.in) ;

			/*
			 * //服务器端循环读取和回复 while(true) { byte[] bys = new byte[1024] ; int len =
			 * in.read(bys) ; String msgStr = new String(bys, 0, len) ;
			 * System.out.println(msgStr);
			 * 
			 * System.out.println("请回复消息:"); String msg = sc.nextLine() ;
			 * out.write(msg.getBytes()); }
			 */

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
