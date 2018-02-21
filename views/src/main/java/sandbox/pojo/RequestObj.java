package sandbox.pojo;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RequestObj {
	@NotNull(message="can't be empty")
	@Size(min = 0, max = 20, message="20 chars. max.")
	private String title;
	@NotNull(message="can't be empty")
	@Size(min = 0, max = 20, message="20 chars. max.")
	private String singer;
	/**
	 * Documentation test
	 * @return
	 */
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSinger() {
		return singer;
	}
	public void setSinger(String singer) {
		this.singer = singer;
	}
	@Override
	public String toString() {
		return "RequestObj [title=" + title + ", singer=" + singer + "]";
	}
}
