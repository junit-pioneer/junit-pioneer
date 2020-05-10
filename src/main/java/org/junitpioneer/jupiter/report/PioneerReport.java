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

import java.util.ArrayList;
import java.util.List;

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
 *         &lt;element ref="{}testcontainer" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{}testcase" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "properties", "testcontainer", "testcase" })
@XmlRootElement(name = "pioneerReport")
public class PioneerReport {

	protected Properties properties;
	protected List<Testcontainer> testcontainer;
	protected List<Testcase> testcase;
	@XmlAttribute(name = "name")
	protected String name;

	public PioneerReport() {
		this.testcontainer = new ArrayList<Testcontainer>();
		this.testcase = new ArrayList<Testcase>();
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
	 * Gets the value of the testcontainer property.
	 *
	 * <p>
	 * This accessor method returns a reference to the live list,
	 * not a snapshot. Therefore any modification you make to the
	 * returned list will be present inside the JAXB object.
	 * This is why there is not a <CODE>set</CODE> method for the testcontainer property.
	 *
	 * <p>
	 * For example, to add a new item, do as follows:
	 * <pre>
	 *    getTestcontainer().add(newItem);
	 * </pre>
	 *
	 *
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link Testcontainer }
	 *
	 *
	 */
	public List<Testcontainer> getTestcontainer() {
		if (testcontainer == null) {
			testcontainer = new ArrayList<Testcontainer>();
		}
		return this.testcontainer;
	}

	/**
	 * Gets the value of the testcase property.
	 *
	 * <p>
	 * This accessor method returns a reference to the live list,
	 * not a snapshot. Therefore any modification you make to the
	 * returned list will be present inside the JAXB object.
	 * This is why there is not a <CODE>set</CODE> method for the testcase property.
	 *
	 * <p>
	 * For example, to add a new item, do as follows:
	 * <pre>
	 *    getTestcase().add(newItem);
	 * </pre>
	 *
	 *
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link Testcase }
	 *
	 *
	 */
	public List<Testcase> getTestcase() {
		if (testcase == null) {
			testcase = new ArrayList<Testcase>();
		}
		return this.testcase;
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

}
