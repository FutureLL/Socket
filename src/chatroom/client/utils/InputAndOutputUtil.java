package chatroom.client.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class InputAndOutputUtil {
	public static byte[] readFile(String path) {
		File file = new File(path);
		// 数组用来保存读取的数据 相当于水池
		byte datas[] = null;
		if (!file.exists()) {
			datas = null;
		} else {
			try {
				// 字节数组输出流 用来往内存中写字节数组 可以用来拼接字节数组
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				// 创建文件输入流
				FileInputStream fis = new FileInputStream(file);
				// 用来保存每次读的数据 相当水瓢(每次读1024字节 但是不一定每次能读这么多 实际读取的长度用len保存)
				byte data[] = new byte[1024 * 1024];
				// 用来保存每次读取的字节大小
				int len = 0;
				// 不断的读取 直到数据读完
				while ((len = fis.read(data)) > 0) {
					// 把每次读入的数据 存放在字节数组流的内存中
					baos.write(data, 0, len);
				}
				// 把字节数组流中的数据转为字节数组
				datas = baos.toByteArray();
				baos.flush();
				baos.close();
				// 关闭流
				fis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return datas;
	}

	public static boolean writeFile(String path, byte datas[]) {
		try {
			File file = new File(path);
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(datas);
			// 倾倒关闭
			fos.flush();
			fos.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
