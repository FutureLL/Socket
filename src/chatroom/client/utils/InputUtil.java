package chatroom.client.utils;

import java.util.Scanner;

public class InputUtil {

	public static int inputIntType(Scanner sc) {
		int choose = 0;
		while (true) {
			try {
				//录入用户输入的整数
				choose = sc.nextInt();
				break;
			} catch (Exception e) {
				//自己处理
				sc = new Scanner(System.in);
				System.out.println("输入的类型不正确，请重新输入:");
			}
		}
		return choose;
	}
}
