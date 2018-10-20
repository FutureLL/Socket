package chatroom.client;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.SocketException;

import chatroom.client.utils.InputAndOutputUtil;
import chatroom.client.utils.TimeUtil;
import chatroom.configs.Configs;

//改进之后:客户端的读服务器端反馈消息的子线程
public class ClientThread extends Thread {
	private InputStream in;

	public ClientThread(InputStream in) {
		this.in = in;
	}

	// 成员位置定义一个变量
	boolean flag = true;

	@Override
	public void run() {

		try {
			// 不断读取
			while (flag) {
				// 读取服务器端反馈的消息
				byte[] bys = new byte[1024 * 10];
				int len = in.read(bys);

				// 客户端接收过来的消息格式:发送者:消息内容:消息类型:系统时间

				String msgStr = new String(bys, 0, len).trim(); // 去除两端空格
				// 以":"开始拆分消息
				String[] msgs = msgStr.split(":");
				String sender = msgs[0];
				String msgContent = msgs[1];
				int msgType = Integer.parseInt(msgs[2]);
				String time = msgs[3];
				
				// String--->long
				long timeLong = Long.parseLong(time);
				// 创建Date日期类对象
				/*
				 * Date date = new Date(timeLong) ; SimpleDateFormat sdf = new
				 * SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ; String timeStr = sdf.format(date) ;
				 */// 日期文本格式
				// 调用工具类,将long类型的毫秒值转换成date类型的时间格式
				String timeStr = TimeUtil.changeMils2Date(timeLong, "yyyy-MM-dd HH:mm:ss");
				
				// 整个业务逻辑都在这里 :客户端的子线程,服务器转发的消息拆分后,做出相应的展示
				if (msgType == Configs.MSG_PRIVATE) {
					// 私聊的逻辑
					System.out.println(timeStr);
					System.out.println(sender + " 对你说: " + msgContent);
				} else if (msgType == Configs.MSG_ONLINE) {
					// 上线提醒
					System.out.println(timeStr);
					System.out.println(sender + " : " + msgContent);
				} else if (msgType == Configs.MSG_PUBLIC) {
					// 公聊的逻辑
					System.out.println(timeStr);
					System.out.println(sender + " 对大家说: " + msgContent);
				} else if (msgType == Configs.MSG_ONLIST) {
					// 在线列表
					System.out.println(timeStr);
					System.out.println("当前在线用户:");
					System.out.println(msgContent);
				} else if (msgType == Configs.MSG_EXIT) {
					// 退出逻辑
					System.out.println(timeStr);
					System.out.println(sender+"☺"+msgContent);
				} else if (msgType == Configs.MSG_FILE) {
					// 显示系统时间
					System.out.println(timeStr);
					// 将文件的名称和文件大小拆分出来
					String[] fileInfo = msgContent.split("#");
					String fileName = fileInfo[0];
					long fileLength = Long.parseLong(fileInfo[1]);

					// 展示内容
					System.out.println(sender + "给你发送来一个文件," + fileName + "文件大小" + (fileLength / 1024) + "KB");

					// 要读文件
					// 使用当前通道内内输入流对象,使用内存操作输出流写
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					byte[] cacheBytes = new byte[1024];
					int cacheLength = 0;
					while (true) {
						int len2 = in.read(cacheBytes);
						bos.write(cacheBytes, 0, len2);

						// 记录读取有效字节数组
						cacheLength += len2;
						if (cacheLength == fileLength) {
							break;
						}
					}

					// 获取文件字节数据
					byte[] fileBytes = bos.toByteArray();
					boolean b = InputAndOutputUtil.writeFile("D:\\" + fileName, fileBytes);
					if (b) {
						// 保存成功
						System.out.println("当前文件保存成功" + "D:\\" + fileName);
						break;
					} else {
						// 失败
						System.out.println("文件保存失败!");
					}
				}
			}

		} catch (SocketException e) {
			// 空处理
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
