package chatroom.client;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

import chatroom.client.utils.InputAndOutputUtil;
import chatroom.client.utils.InputUtil;
import chatroom.configs.Configs;

//客户端

/**
 * 当前程序设计不合理 原因: 将客户端,以及服务端,发消息和读消息都写在一个类中,可能会出现问题:消息阻塞的现象,
 * 客户端发送端消息,如果内容够大,服务器端没有加载完,客户端还需要服务器端反馈,这个可能出现问题!
 * 
 * 并且Java开发原则:低耦合,高内聚, 可以将部分内容放到子线程中,关键是把发送消息放在子线程还是读消息放在子线程中呢?
 * 一般情况:将读消息放在线程,因为在 子线程中,一般不键盘录入的!
 * 
 * 改进方案:客户端和服务器端分别开启两个读消息的子线程中
 */
public class ClientChatRoom {

	private static Scanner sc;

	private static InputStream in;
	// private static ObjectInputStream in ;

	private static OutputStream out;
	// private static ObjectOutputStream out ;

	public static void main(String[] args) {

		try {
			// 使用TCP编程
			// 创建客户端的Socket对象
			Socket s = new Socket("192.168.159.1",8888);

			//创建通道内的流对象,用来传送用户名,以及接收服务器的反馈
			out = s.getOutputStream();
			in = s.getInputStream();
			
			// 改进:使用序列化流和反序列化流包装
			// ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream()) ;
			// ObjectInputStream in = new ObjectInputStream(s.getInputStream());

			// 创建键盘录入对象,输入用户名
			sc = new Scanner(System.in);

			// 客户端注册用户名
			// 不断的注册
			while (true) {
				System.out.println("请您输入您要注册的用户名:"); // 张三 ---->数组角标越界
				String username = sc.nextLine();

				// 使用通道内的输出流,将用户名写给服务器端
				out.write(username.getBytes());

				// 接收服务器端的反馈
				// 读取保存用户名线程的一个反馈
				byte[] bys = new byte[1024];
				int len = in.read(bys);
				String fkName = new String(bys, 0, len);

				//进行判断
				if (fkName.equals("yes")) {
					System.out.println("用户名注册成功....");
					break;
				} else if (fkName.equals("no")) {
					System.out.println("用户名已经存在,请重新注册");
				}

			}

			// 开启客户端的子线程
			ClientThread ct = new ClientThread(in);
			ct.start();

			// 给用户提供选择
			// 定义一个变量:
			boolean flag = true;
			while (flag) {
				System.out.println("请输入您的选择:1 私聊,2 公聊 3 在线列表 ,4 退出  ,5 发送文件 ,6 在线隐身");
				// int num = sc.nextInt() ;
				// 使用工具类InputUtil中的方法inputIntType(),用来判断键盘的输入是否满足要求
				int num = InputUtil.inputIntType(new Scanner(System.in));

				// 使用switch语句,给用户提供选项
				switch (num) {
				case 1:// 私聊
					privateTalk();
					break;
				case 2:// 公聊
					publicTalk();
					break;
				case 3:// 在线列表
					getOnList();
					break;
				case 4:// 退出 在客户端应该显示谁谁下线了....
					exitTalk();
					// 修改flag变量
					
					flag = false;// 跳出菜单 为了关闭Socket对象

					// 停掉子线程
					ct.flag = false;
					break;
				case 5:// 发送文件
					sendFile();
					break;
				}
			}

			s.close(); // 关闭客户端的Socket对象

		} catch (SocketException e) { // 关闭socket对象的时候,本身就抛出一个异常,会将SocketException异常打印控制台上,不好看
			// 为了让控制台不出现异常的消息字符串,所以空处理!
			// 空处理
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		}

	}

	// 发送文件
	private static void sendFile() throws IOException {
		// 思路:
		// 将发送的消息以及文件通过某种方式拼接成一个大的字节数组发送过去
		// 规定:发送整个字节的大小10kb,文件内容可能不够10kb,用空字节补齐
		// 用空字节数组,服务器端读到内容,去除两端空格

		System.out.println("请输入目标用户:");
		String mbUser = sc.nextLine();
		System.out.println("请输入文件路径:");
		String filePath = sc.nextLine();

		// 将文件封装成File对象
		File file = new File(filePath);
		// 组装消息: 接收者:消息内容:消息类型 (文件:文件名称,和文件的大小)
		String msg = mbUser + ":" + (file.getName() + "#" + file.length()) + ":" + Configs.MSG_FILE;

		// 将msg转成消息字节数组
		byte[] msgBytes = msg.getBytes();
		// 空字节数组
		byte[] emptyBytes = new byte[1024 * 10 - msgBytes.length];
		// 获取文件的大小字节数组
		byte[] fileBytes = InputAndOutputUtil.readFile(filePath);
		// 将上面三个字节数组在内存中拼接成一个大的字节数组--->发送过去
		// 创建内存操作流(针对文件不宜过大)
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		// 将上面字节字节写到对象对象中
		bos.write(msgBytes);
		bos.write(emptyBytes);
		bos.write(fileBytes);

		// 内存操作流中已经有这几个字节数组
		// public byte[] toByteArray()
		byte[] allBytes = bos.toByteArray();

		out.write(allBytes);

	}

	private static void exitTalk() throws IOException {

		// 思路:
		// 客户端要做的事情:停掉客户端所在的Socket,并且将读取服务器端转发消息的子线程停掉(ClientThread)
		// 服务器端要做的事情:停电服务器端读取消息的子线程ServetThread,并且将用户从集合中移出用户自己(username),给其他人XX 下线了..

		// 发送的消息格式:接收者:消息内容:消息类型
		String msg = "null" + ":" + "null" + ":" + Configs.MSG_EXIT;
		out.write(msg.getBytes());

	}

	// 在线列表
	private static void getOnList() throws IOException {

		// 约定的消息格式:接收者:消息内容:消息类型
		// 组装消息
		// 发送消息到服务器读取消息的子线程中
		String msg = "null" + ":" + "null" + ":" + Configs.MSG_ONLIST;
		out.write(msg.getBytes());

	}

	// 公聊
	private static void publicTalk() throws IOException {
		while (true) {
			// 约定的消息格式:接收者:消息内容:消息类型
			System.out.println("当前您处于公聊模式(消息内容)  -q 退出当前模式 ");
			String msg = sc.nextLine();
			if ("-q".equals(msg)) {
				break;
			}

			// 约定了消息格式,所以这里的接收者用null代替
			// 组装消息 接收者:消息内容:消息类型
			msg = "null" + ":" + msg + ":" + Configs.MSG_PUBLIC;

			// 使用流对象发送过去
			out.write(msg.getBytes());
		}
	}

	// 私聊
	private static void privateTalk() throws IOException {
		while (true) {

			// 约定的新的消息格式 接收者:消息内容:消息类型
			System.out.println("当前您处于私聊模式(接收者:消息内容)  -q 退出当前模式 ");
			String msg = sc.nextLine();
			if ("-q".equals(msg)) {
				break;
			}

			// 约定消息格式:接收者:消息内容:消息类型
			// 客户端组装消息
			msg = msg + ":" + Configs.MSG_PRIVATE;

			// 发送到服务器端
			out.write(msg.getBytes());

			// out.writeObject(obj);

		}
	}
}
