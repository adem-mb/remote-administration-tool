package application.Association;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

public class BrowserItem extends RecursiveTreeObject<BrowserItem>{
	public String FileName,size,date;
	public String type;
	public NameType nameType;
	
	public String getFileName() {
		return FileName;
	}

	public void setFileName(String fileName) {
		FileName = fileName;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public BrowserItem(String fileName, String date, String sizen,String type) {
		this.type=type;
		this.FileName = fileName;
		this.size = sizen;
		this.date = date;
		nameType=new NameType(FileName, type);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public NameType getNameType() {
		return nameType;
	}

	public void setNameType(NameType nameType) {
		this.nameType = nameType;
	}

	public static class NameType{
		
		String name,type;

		public NameType(String name, String type) {
			super();
			this.name = name;
			this.type = type;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}
		
		
	}
	

	

}
