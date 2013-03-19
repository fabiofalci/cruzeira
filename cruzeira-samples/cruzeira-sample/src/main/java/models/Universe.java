package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@Entity(name = "UNIVERSE")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Universe {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column
	private String universe;

	@Override
	public String toString() {
		return "Universe " + id + " " + universe;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setUniverse(String universe) {
		this.universe = universe;
	}

	public String getUniverse() {
		return universe;
	}
}
