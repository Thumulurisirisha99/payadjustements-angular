package com.earnings.payadjustements.entity;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class IdEntity implements Serializable {
	/**
	 * 
	 */
	private Integer id;

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	public IdEntity() {

	}

	public IdEntity(Integer id) {
		this.id = id;

	}

}
