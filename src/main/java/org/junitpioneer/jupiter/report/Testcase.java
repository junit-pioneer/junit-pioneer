/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.report;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{}properties" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="status" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "properties" })
@XmlRootElement(name = "testcase")
public class Testcase {

	protected Properties properties;
	@XmlAttribute(name = "name", required = true)
	protected String name;
	@XmlAttribute(name = "status")
	protected String status;

	public Testcase() {
	}

	public Testcase(String name, String status) {
		this.name = name;
		this.status = status;
	}

	/**
	 * Gets the value of the properties property.
	 *
	 * @return
	 *     possible object is
	 *     {@link Properties }
	 *
	 */
	public Properties getProperties() {
		return properties;
	}

	/**
	 * Sets the value of the properties property.
	 *
	 * @param value
	 *     allowed object is
	 *     {@link Properties }
	 *
	 */
	public void setProperties(Properties value) {
		this.properties = value;
	}

	/**
	 * Gets the value of the name property.
	 *
	 * @return
	 *     possible object is
	 *     {@link String }
	 *
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the value of the name property.
	 *
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *
	 */
	public void setName(String value) {
		this.name = value;
	}

	/**
	 * Gets the value of the status property.
	 *
	 * @return
	 *     possible object is
	 *     {@link String }
	 *
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Sets the value of the status property.
	 *
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *
	 */
	public void setStatus(String value) {
		this.status = value;
	}

}
