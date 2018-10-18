package Server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.SynchronousQueue;

import javax.sound.sampled.Port;

import org.omg.CORBA.PUBLIC_MEMBER;

import DataBaseOperation.OperateSQLServer;

public class ParseInformation {
	int statement;
	String Nickname;
	String userID;
	String time;
	String information;
	InetAddress address;
	OperateSQLServer operateSQLServer;
	public ParseInformation(InetAddress address, byte[] information) throws UnknownHostException {
		//final int PORT = 23333;
		this.address = address;
		operateSQLServer = new OperateSQLServer();
		int maxSplit = 4;
		String sourceStr = new String(information);
		String[] sourceStrArray = sourceStr.split("#",maxSplit);
		statement = Integer.parseInt(sourceStrArray[0]);
		userID = sourceStrArray[1];
		time = sourceStrArray[2];
		this.information = sourceStrArray[3];
		switch(statement) {
		case 1: textToServer(this.information); break;
		case 2: LogIn(this.information); break;
		case 3: logOut(this.information); break;
		case 4: isOnlineToServer(this.information); break;
		case 5: textToUser(userID); break;
		case 6: isOnlineToUser(this.information); break;
		case 7: resetProfileToServer(this.information); break;
		case 8: resetPswdToServer(this.information); break;
		case 9: deleteContact(this.information); break;
		case 10: addContact(this.information); break;
		default: break;
		}
	}
	//1+发送者ID+时间+目标ID+聊天文本消息
	public void textToServer(String information) {
		int maxSplit = 2;
		String[] sourceStrArray = information.split("#",maxSplit);
		int targetID = Integer.parseInt(sourceStrArray[0]);
		information = sourceStrArray[1];
		information = "5#"+userID+"#"+time+"#"+information;
		operateSQLServer.connectToDatabase();
		String IP = operateSQLServer.getContactsIP(targetID);
//		System.out.println(IP);
//		System.out.println(information);
//		IP = "127.0.0.1";
		try {
			if(IP != null) {
				SendThread sendThread = new SendThread(IP, 23333, information.getBytes());
				new Thread(sendThread).start();
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		operateSQLServer.closeDatabase();
	}
	//2+发送者ID+时间+登录账号+密码
	public void LogIn(String information) throws UnknownHostException {
		int maxSplit = 2;
		String[] sourceStrArray = information.split("#",maxSplit);
		int userID = Integer.parseInt(sourceStrArray[0]);
		String password = sourceStrArray[1];
		userID = 33;
		password = "123";
		operateSQLServer.connectToDatabase();
		ResultSet resultSet = operateSQLServer.getPersonalInformation(userID);
		String IP = address.getHostAddress();
		try {			
			resultSet.next();
			if(resultSet != null) {
				if(password.equals(resultSet.getString(3))) {
					operateSQLServer.addUserToOnlineUsers(IP, userID);
					SendThread st = new SendThread(IP,23334,"2".getBytes());
					new Thread(st);
				}
				else {
					SendThread st = new SendThread(IP,23334,"1".getBytes());
					new Thread(st);
				}
			}	
		} catch (SQLException e) {
			SendThread st = new SendThread(IP,23334,"0".getBytes());
			new Thread(st);
			e.printStackTrace();
		}
		operateSQLServer.closeDatabase();
	}
	//3+发送者ID+时间+退出账号
	public void logOut(String information) {
		operateSQLServer.connectToDatabase();
		int userID = Integer.parseInt(information);
		operateSQLServer.deleteUserToOnlineUsers(userID);
		operateSQLServer.closeDatabase();
	}
	//4+发送者ID+时间+在线状态反馈码
	public void isOnlineToServer(String information) {}
	//5+发送者ID+时间+聊天文本信息
	public void textToUser(String userID) {
		Nickname = operateSQLServer.getContactsNickname(32, Integer.parseInt(userID));
	}
	//6+发送者ID+时间+在线状态检测码
	public void isOnlineToUser(String information) {}
	//7+发送者ID+时间+更改个人资料信息
	public void resetProfileToServer(String information) {
		int maxSplit = 5;
		String[] sourceStrArray = information.split("#",maxSplit);
		String nickName = sourceStrArray[0]; 
		String sex = sourceStrArray[1];
		String birthday = sourceStrArray[2];
		String mailBox = sourceStrArray[3];
		String personalSignature = sourceStrArray[4];
		operateSQLServer.connectToDatabase();
		operateSQLServer.updateUserImformation(Integer.parseInt(this.userID), nickName, sex, birthday, mailBox, personalSignature);
		operateSQLServer.closeDatabase();
	}
	//8+发送者ID+时间+修改个人密码
	public void resetPswdToServer(String information) {
		String password = information;
		operateSQLServer.connectToDatabase();
		operateSQLServer.updateUserPassword(Integer.parseInt(this.userID), password);
		operateSQLServer.closeDatabase();
	}
	//9+发送者ID+时间+删除好友请求contactsID
	public void deleteContact(String information) {
		int contactsID = Integer.parseInt(information);
		int userID = Integer.parseInt(this.userID);
		operateSQLServer.connectToDatabase();
		operateSQLServer.deleteContacts(userID, contactsID);
		operateSQLServer.closeDatabase();
	}
	//10+发送者ID+时间+添加好友请求contactsID
	public void addContact(String information) {
		int maxSplit = 2;
		String[] sourceStrArray = information.split("#",maxSplit);
		int contactsID = Integer.parseInt(sourceStrArray[0]);
		String remark = sourceStrArray[1];
		int userID = Integer.parseInt(this.userID);
		operateSQLServer.connectToDatabase();
		operateSQLServer.addNewContacts(userID, contactsID, remark);
		operateSQLServer.closeDatabase();
	}
	
	public static void main(String args[]) {
		InetAddress address;
		try {
			address = InetAddress.getByName("128.0.0.1");
//			ParseInformation p = new ParseInformation(address, "7#34#123#q#男#3-5#@qq.com#你就是你".getBytes());
//			ParseInformation p = new ParseInformation(address, "8#34#123#534".getBytes());
//			ParseInformation p = new ParseInformation(address, "9#33#123#34".getBytes());
//			ParseInformation p = new ParseInformation(address, "10#33#123#35#备注小猫".getBytes());
//			p.LogIn("123#123#213");
//			p.logOut("33");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
	}
	
}
