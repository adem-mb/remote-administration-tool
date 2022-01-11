package application.Utils;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.sun.javafx.stage.StageHelper;

import application.Association.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;

public class Functions {
public static Stage getStage(String type,int sock){
	
	ObservableList<Stage> itemList = StageHelper.getStages();
	for(Stage stage : itemList){
		UserData userdata =(UserData) stage.getUserData();
		if(userdata !=null)
		if(userdata.getSock().equals(String.valueOf(sock)) & userdata.getType().equals(type) )
			return stage;
		
	}
	return null;
}
public static int Byte2Int(byte[] number){

		ByteBuffer wrapped = ByteBuffer.wrap(number); 
		return wrapped.getInt();
	
}
public static void CloseStages(int sock){
	
	ObservableList<Stage> itemList = StageHelper.getStages();
	Stage Stages[];
	for(Stage stage : itemList){
		UserData userdata =(UserData) stage.getUserData();
		if(userdata !=null)
		if(userdata.getSock().equals(String.valueOf(sock)))
			Platform.runLater(
					  () -> {
					  stage.close();
					  }
					);
		
	}
	
}
public static TreeItem<ConnectionItem> find_group(TreeItem<ConnectionItem> item,String group_name){

	  for (TreeItem<ConnectionItem> child : item.getChildren()){
	  
	   if(child.getValue().getGroup().equals(group_name))
		   return child;

	  }
	  
	
	return null;
}
public static TreeItem<ConnectionItem> find_user(TreeItem<ConnectionItem> item , String value) 
{
  if (item != null && item.getValue().getSock().equals(value))
    return  item;

  for (TreeItem<ConnectionItem> child : item.getChildren()){
   TreeItem<ConnectionItem> s=find_user(child, value);
   if(s!=null)
       return s;
   
  }
  return null;
}
public static Tab createTab(String title ,String id){
	Tab t=new Tab(title);
	t.setId(id);
	
	return t;
}
public static Tab getTab(TabPane tabpane,String type,String id ){
	
	
	ObservableList<Tab> tabs=tabpane.getTabs();
	for(Tab tab:tabs){
		if(tab.getId().equals(type+id))
			return tab;
		
	}
	
	
	return null;
	
	
}

public static byte[] Str2Byte(String s){
	
	
	try {
		return s.getBytes("ISO-8859-15");
	} catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return null;
	
}
private static double rou(double nu){
	return Math.round(nu * 100.0) / 100.0;
}
public static String calculatesize(long size){
	if(size<1024)
		return String.valueOf(size)+" bytes";
	else if((size >=1024) & (size <1024*1024))
		return String.valueOf(rou(size/1024))+" KB";
	else if((size >=1024*1024) & (size <1024*1024*1024))
		return String.valueOf(rou(size/(1024*1024)))+" MB";
	else if(((long)size >=1024*1024*1024) & (size <Math.pow(1024, 4)))
		return String.valueOf(rou(size/(Math.pow(1024, 3))))+" GB";
	else 
	
		return String.valueOf(rou(size/Math.pow(1024, 4)))+" TB";
		
	
}
public static String Byte2Str(byte[] b){
	
	
	try {
		return new String(b,"ISO-8859-15");
	} catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return null;
	
}
public static boolean isMatch(byte[] pattern, byte[] input, int pos) {
    for(int i=0; i< pattern.length; i++) {
        if(pattern[i] != input[pos+i]) {
            return false;
        }
    }
    return true;
}

public static List<byte[]> split(byte[] pattern, byte[] input) {
    List<byte[]> l = new LinkedList<byte[]>();
    int blockStart = 0;
    for(int i=0; i<input.length; i++) {
       if(isMatch(pattern,input,i)) {
          l.add(Arrays.copyOfRange(input, blockStart, i));
          blockStart = i+pattern.length;
          i = blockStart;
       }
    }
    l.add(Arrays.copyOfRange(input, blockStart, input.length ));
    
    return l;
}
public static List<byte[]> tokens(byte[] array, byte[] delimiter) {
    List<byte[]> byteArrays = new LinkedList<>();
    if (delimiter.length == 0) {
        return byteArrays;
    }
    int begin = 0;

    outer:
    for (int i = 0; i < array.length - delimiter.length + 1; i++) {
        for (int j = 0; j < delimiter.length; j++) {
            if (array[i + j] != delimiter[j]) {
                continue outer;
            }
        }
        byteArrays.add(Arrays.copyOfRange(array, begin, i));
        begin = i + delimiter.length;
    }
    byteArrays.add(Arrays.copyOfRange(array, begin, array.length));
    return byteArrays;
}
}
