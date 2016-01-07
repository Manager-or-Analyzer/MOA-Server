package kr.co.data;

import java.util.ArrayList;

public class HtmlData {
	public String userid;
	public String url;
	public String height;
	public String width;
	
	public String time;
	public String doc;
	public ArrayList<HtmlData> children;
	public String purl;
	
	@Override
	public String toString() {
		return "HtmlData [userid=" + userid + ", url=" + url + ", height=" + height + ", width=" + width + ", time="
				+ time + ", doc=" + doc + ", children=" + children + ", purl=" + purl + "]";
	}
}