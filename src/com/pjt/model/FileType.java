package com.pjt.model;

public enum FileType {

	JPG(650, "jpg"),JPEG(650, "jpeg"), GIF(14, "gif"), BMP(50, "bmp"), Dummy(0, ""), TXT(0, "txt"),PNG(14, "png"),EXE(0,"exe"),PDF(0,"pdf");

	private int headerByteLength;
	private String extension;

	private FileType(int headerByteLength, String extension) {
		this.headerByteLength = headerByteLength;
		this.extension = extension;
	}

	public int getHeaderByteLength() {
		return headerByteLength;
	}

	public String getExtension() {
		return extension;
	}

	public static final FileType getFileType(String fileExt) {
		for(FileType ft : FileType.values()) {
			if(ft.getExtension().equalsIgnoreCase(fileExt))
				return ft;
		}
		return Dummy;
	}
}
