package uk.gov.ons.ctp.response.collection.exercise.lib.party.definition;

import com.kscs.util.jaxb.Buildable;
import com.kscs.util.jaxb.PropertyTree;
import com.kscs.util.jaxb.PropertyTreeUse;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for PartyCreationRequestAttributesDTO complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="PartyCreationRequestAttributesDTO"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="checkletter" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="frosic92" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="rusic92" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="frosic2007" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="rusic2007" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="froempment" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="frotover" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="entref" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="legalstatus" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="entrepmkr" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="region" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="birthdate" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="entname1" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="entname2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="entname3" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="runame1" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="runame2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="runame3" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="tradstyle1" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="tradstyle2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="tradstyle3" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="seltype" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="inclexcl" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="cell_no" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="formtype" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="currency" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="sampleUnitId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "PartyCreationRequestAttributesDTO",
    propOrder = {
      "checkletter",
      "frosic92",
      "rusic92",
      "frosic2007",
      "rusic2007",
      "froempment",
      "frotover",
      "entref",
      "legalstatus",
      "name",
      "entrepmkr",
      "region",
      "birthdate",
      "entname1",
      "entname2",
      "entname3",
      "runame1",
      "runame2",
      "runame3",
      "tradstyle1",
      "tradstyle2",
      "tradstyle3",
      "seltype",
      "inclexcl",
      "cellNo",
      "formtype",
      "currency",
      "sampleUnitId"
    })
public class PartyCreationRequestAttributesDTO {

  @XmlElement(required = true)
  protected String checkletter;

  @XmlElement(required = true)
  protected String frosic92;

  @XmlElement(required = true)
  protected String rusic92;

  @XmlElement(required = true)
  protected String frosic2007;

  @XmlElement(required = true)
  protected String rusic2007;

  protected int froempment;
  protected int frotover;

  @XmlElement(required = true)
  protected String entref;

  @XmlElement(required = true)
  protected String legalstatus;

  protected String name;

  @XmlElement(required = true)
  protected String entrepmkr;

  @XmlElement(required = true)
  protected String region;

  @XmlElement(required = true)
  protected String birthdate;

  @XmlElement(required = true)
  protected String entname1;

  protected String entname2;
  protected String entname3;

  @XmlElement(required = true)
  protected String runame1;

  protected String runame2;
  protected String runame3;
  protected String tradstyle1;
  protected String tradstyle2;
  protected String tradstyle3;

  @XmlElement(required = true)
  protected String seltype;

  protected String inclexcl;

  @XmlElement(name = "cell_no")
  protected int cellNo;

  @XmlElement(required = true)
  protected String formtype;

  protected String currency;
  protected String sampleUnitId;

  /** Default no-arg constructor */
  public PartyCreationRequestAttributesDTO() {
    super();
  }

  /** Fully-initialising value constructor */
  public PartyCreationRequestAttributesDTO(
      final String checkletter,
      final String frosic92,
      final String rusic92,
      final String frosic2007,
      final String rusic2007,
      final int froempment,
      final int frotover,
      final String entref,
      final String legalstatus,
      final String name,
      final String entrepmkr,
      final String region,
      final String birthdate,
      final String entname1,
      final String entname2,
      final String entname3,
      final String runame1,
      final String runame2,
      final String runame3,
      final String tradstyle1,
      final String tradstyle2,
      final String tradstyle3,
      final String seltype,
      final String inclexcl,
      final int cellNo,
      final String formtype,
      final String currency,
      final String sampleUnitId) {
    this.checkletter = checkletter;
    this.frosic92 = frosic92;
    this.rusic92 = rusic92;
    this.frosic2007 = frosic2007;
    this.rusic2007 = rusic2007;
    this.froempment = froempment;
    this.frotover = frotover;
    this.entref = entref;
    this.legalstatus = legalstatus;
    this.name = name;
    this.entrepmkr = entrepmkr;
    this.region = region;
    this.birthdate = birthdate;
    this.entname1 = entname1;
    this.entname2 = entname2;
    this.entname3 = entname3;
    this.runame1 = runame1;
    this.runame2 = runame2;
    this.runame3 = runame3;
    this.tradstyle1 = tradstyle1;
    this.tradstyle2 = tradstyle2;
    this.tradstyle3 = tradstyle3;
    this.seltype = seltype;
    this.inclexcl = inclexcl;
    this.cellNo = cellNo;
    this.formtype = formtype;
    this.currency = currency;
    this.sampleUnitId = sampleUnitId;
  }

  /**
   * Gets the value of the checkletter property.
   *
   * @return possible object is {@link String }
   */
  public String getCheckletter() {
    return checkletter;
  }

  /**
   * Sets the value of the checkletter property.
   *
   * @param value allowed object is {@link String }
   */
  public void setCheckletter(String value) {
    this.checkletter = value;
  }

  /**
   * Gets the value of the frosic92 property.
   *
   * @return possible object is {@link String }
   */
  public String getFrosic92() {
    return frosic92;
  }

  /**
   * Sets the value of the frosic92 property.
   *
   * @param value allowed object is {@link String }
   */
  public void setFrosic92(String value) {
    this.frosic92 = value;
  }

  /**
   * Gets the value of the rusic92 property.
   *
   * @return possible object is {@link String }
   */
  public String getRusic92() {
    return rusic92;
  }

  /**
   * Sets the value of the rusic92 property.
   *
   * @param value allowed object is {@link String }
   */
  public void setRusic92(String value) {
    this.rusic92 = value;
  }

  /**
   * Gets the value of the frosic2007 property.
   *
   * @return possible object is {@link String }
   */
  public String getFrosic2007() {
    return frosic2007;
  }

  /**
   * Sets the value of the frosic2007 property.
   *
   * @param value allowed object is {@link String }
   */
  public void setFrosic2007(String value) {
    this.frosic2007 = value;
  }

  /**
   * Gets the value of the rusic2007 property.
   *
   * @return possible object is {@link String }
   */
  public String getRusic2007() {
    return rusic2007;
  }

  /**
   * Sets the value of the rusic2007 property.
   *
   * @param value allowed object is {@link String }
   */
  public void setRusic2007(String value) {
    this.rusic2007 = value;
  }

  /** Gets the value of the froempment property. */
  public int getFroempment() {
    return froempment;
  }

  /** Sets the value of the froempment property. */
  public void setFroempment(int value) {
    this.froempment = value;
  }

  /** Gets the value of the frotover property. */
  public int getFrotover() {
    return frotover;
  }

  /** Sets the value of the frotover property. */
  public void setFrotover(int value) {
    this.frotover = value;
  }

  /**
   * Gets the value of the entref property.
   *
   * @return possible object is {@link String }
   */
  public String getEntref() {
    return entref;
  }

  /**
   * Sets the value of the entref property.
   *
   * @param value allowed object is {@link String }
   */
  public void setEntref(String value) {
    this.entref = value;
  }

  /**
   * Gets the value of the legalstatus property.
   *
   * @return possible object is {@link String }
   */
  public String getLegalstatus() {
    return legalstatus;
  }

  /**
   * Sets the value of the legalstatus property.
   *
   * @param value allowed object is {@link String }
   */
  public void setLegalstatus(String value) {
    this.legalstatus = value;
  }

  /**
   * Gets the value of the name property.
   *
   * @return possible object is {@link String }
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the value of the name property.
   *
   * @param value allowed object is {@link String }
   */
  public void setName(String value) {
    this.name = value;
  }

  /**
   * Gets the value of the entrepmkr property.
   *
   * @return possible object is {@link String }
   */
  public String getEntrepmkr() {
    return entrepmkr;
  }

  /**
   * Sets the value of the entrepmkr property.
   *
   * @param value allowed object is {@link String }
   */
  public void setEntrepmkr(String value) {
    this.entrepmkr = value;
  }

  /**
   * Gets the value of the region property.
   *
   * @return possible object is {@link String }
   */
  public String getRegion() {
    return region;
  }

  /**
   * Sets the value of the region property.
   *
   * @param value allowed object is {@link String }
   */
  public void setRegion(String value) {
    this.region = value;
  }

  /**
   * Gets the value of the birthdate property.
   *
   * @return possible object is {@link String }
   */
  public String getBirthdate() {
    return birthdate;
  }

  /**
   * Sets the value of the birthdate property.
   *
   * @param value allowed object is {@link String }
   */
  public void setBirthdate(String value) {
    this.birthdate = value;
  }

  /**
   * Gets the value of the entname1 property.
   *
   * @return possible object is {@link String }
   */
  public String getEntname1() {
    return entname1;
  }

  /**
   * Sets the value of the entname1 property.
   *
   * @param value allowed object is {@link String }
   */
  public void setEntname1(String value) {
    this.entname1 = value;
  }

  /**
   * Gets the value of the entname2 property.
   *
   * @return possible object is {@link String }
   */
  public String getEntname2() {
    return entname2;
  }

  /**
   * Sets the value of the entname2 property.
   *
   * @param value allowed object is {@link String }
   */
  public void setEntname2(String value) {
    this.entname2 = value;
  }

  /**
   * Gets the value of the entname3 property.
   *
   * @return possible object is {@link String }
   */
  public String getEntname3() {
    return entname3;
  }

  /**
   * Sets the value of the entname3 property.
   *
   * @param value allowed object is {@link String }
   */
  public void setEntname3(String value) {
    this.entname3 = value;
  }

  /**
   * Gets the value of the runame1 property.
   *
   * @return possible object is {@link String }
   */
  public String getRuname1() {
    return runame1;
  }

  /**
   * Sets the value of the runame1 property.
   *
   * @param value allowed object is {@link String }
   */
  public void setRuname1(String value) {
    this.runame1 = value;
  }

  /**
   * Gets the value of the runame2 property.
   *
   * @return possible object is {@link String }
   */
  public String getRuname2() {
    return runame2;
  }

  /**
   * Sets the value of the runame2 property.
   *
   * @param value allowed object is {@link String }
   */
  public void setRuname2(String value) {
    this.runame2 = value;
  }

  /**
   * Gets the value of the runame3 property.
   *
   * @return possible object is {@link String }
   */
  public String getRuname3() {
    return runame3;
  }

  /**
   * Sets the value of the runame3 property.
   *
   * @param value allowed object is {@link String }
   */
  public void setRuname3(String value) {
    this.runame3 = value;
  }

  /**
   * Gets the value of the tradstyle1 property.
   *
   * @return possible object is {@link String }
   */
  public String getTradstyle1() {
    return tradstyle1;
  }

  /**
   * Sets the value of the tradstyle1 property.
   *
   * @param value allowed object is {@link String }
   */
  public void setTradstyle1(String value) {
    this.tradstyle1 = value;
  }

  /**
   * Gets the value of the tradstyle2 property.
   *
   * @return possible object is {@link String }
   */
  public String getTradstyle2() {
    return tradstyle2;
  }

  /**
   * Sets the value of the tradstyle2 property.
   *
   * @param value allowed object is {@link String }
   */
  public void setTradstyle2(String value) {
    this.tradstyle2 = value;
  }

  /**
   * Gets the value of the tradstyle3 property.
   *
   * @return possible object is {@link String }
   */
  public String getTradstyle3() {
    return tradstyle3;
  }

  /**
   * Sets the value of the tradstyle3 property.
   *
   * @param value allowed object is {@link String }
   */
  public void setTradstyle3(String value) {
    this.tradstyle3 = value;
  }

  /**
   * Gets the value of the seltype property.
   *
   * @return possible object is {@link String }
   */
  public String getSeltype() {
    return seltype;
  }

  /**
   * Sets the value of the seltype property.
   *
   * @param value allowed object is {@link String }
   */
  public void setSeltype(String value) {
    this.seltype = value;
  }

  /**
   * Gets the value of the inclexcl property.
   *
   * @return possible object is {@link String }
   */
  public String getInclexcl() {
    return inclexcl;
  }

  /**
   * Sets the value of the inclexcl property.
   *
   * @param value allowed object is {@link String }
   */
  public void setInclexcl(String value) {
    this.inclexcl = value;
  }

  /** Gets the value of the cellNo property. */
  public int getCellNo() {
    return cellNo;
  }

  /** Sets the value of the cellNo property. */
  public void setCellNo(int value) {
    this.cellNo = value;
  }

  /**
   * Gets the value of the formtype property.
   *
   * @return possible object is {@link String }
   */
  public String getFormtype() {
    return formtype;
  }

  /**
   * Sets the value of the formtype property.
   *
   * @param value allowed object is {@link String }
   */
  public void setFormtype(String value) {
    this.formtype = value;
  }

  /**
   * Gets the value of the currency property.
   *
   * @return possible object is {@link String }
   */
  public String getCurrency() {
    return currency;
  }

  /**
   * Sets the value of the currency property.
   *
   * @param value allowed object is {@link String }
   */
  public void setCurrency(String value) {
    this.currency = value;
  }

  /**
   * Gets the value of the sampleUnitId property.
   *
   * @return possible object is {@link String }
   */
  public String getSampleUnitId() {
    return sampleUnitId;
  }

  /**
   * Sets the value of the sampleUnitId property.
   *
   * @param value allowed object is {@link String }
   */
  public void setSampleUnitId(String value) {
    this.sampleUnitId = value;
  }

  /**
   * Copies all state of this object to a builder. This method is used by the {@link #copyOf} method
   * and should not be called directly by client code.
   *
   * @param _other A builder instance to which the state of this object will be copied.
   */
  public <_B> void copyTo(final PartyCreationRequestAttributesDTO.Builder<_B> _other) {
    _other.checkletter = this.checkletter;
    _other.frosic92 = this.frosic92;
    _other.rusic92 = this.rusic92;
    _other.frosic2007 = this.frosic2007;
    _other.rusic2007 = this.rusic2007;
    _other.froempment = this.froempment;
    _other.frotover = this.frotover;
    _other.entref = this.entref;
    _other.legalstatus = this.legalstatus;
    _other.name = this.name;
    _other.entrepmkr = this.entrepmkr;
    _other.region = this.region;
    _other.birthdate = this.birthdate;
    _other.entname1 = this.entname1;
    _other.entname2 = this.entname2;
    _other.entname3 = this.entname3;
    _other.runame1 = this.runame1;
    _other.runame2 = this.runame2;
    _other.runame3 = this.runame3;
    _other.tradstyle1 = this.tradstyle1;
    _other.tradstyle2 = this.tradstyle2;
    _other.tradstyle3 = this.tradstyle3;
    _other.seltype = this.seltype;
    _other.inclexcl = this.inclexcl;
    _other.cellNo = this.cellNo;
    _other.formtype = this.formtype;
    _other.currency = this.currency;
    _other.sampleUnitId = this.sampleUnitId;
  }

  public <_B> PartyCreationRequestAttributesDTO.Builder<_B> newCopyBuilder(
      final _B _parentBuilder) {
    return new PartyCreationRequestAttributesDTO.Builder<_B>(_parentBuilder, this, true);
  }

  public PartyCreationRequestAttributesDTO.Builder<Void> newCopyBuilder() {
    return newCopyBuilder(null);
  }

  public static PartyCreationRequestAttributesDTO.Builder<Void> builder() {
    return new PartyCreationRequestAttributesDTO.Builder<Void>(null, null, false);
  }

  public static <_B> PartyCreationRequestAttributesDTO.Builder<_B> copyOf(
      final PartyCreationRequestAttributesDTO _other) {
    final PartyCreationRequestAttributesDTO.Builder<_B> _newBuilder =
        new PartyCreationRequestAttributesDTO.Builder<_B>(null, null, false);
    _other.copyTo(_newBuilder);
    return _newBuilder;
  }

  /**
   * Copies all state of this object to a builder. This method is used by the {@link #copyOf} method
   * and should not be called directly by client code.
   *
   * @param _other A builder instance to which the state of this object will be copied.
   */
  public <_B> void copyTo(
      final PartyCreationRequestAttributesDTO.Builder<_B> _other,
      final PropertyTree _propertyTree,
      final PropertyTreeUse _propertyTreeUse) {
    final PropertyTree checkletterPropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("checkletter"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (checkletterPropertyTree != null)
        : ((checkletterPropertyTree == null) || (!checkletterPropertyTree.isLeaf())))) {
      _other.checkletter = this.checkletter;
    }
    final PropertyTree frosic92PropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("frosic92"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (frosic92PropertyTree != null)
        : ((frosic92PropertyTree == null) || (!frosic92PropertyTree.isLeaf())))) {
      _other.frosic92 = this.frosic92;
    }
    final PropertyTree rusic92PropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("rusic92"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (rusic92PropertyTree != null)
        : ((rusic92PropertyTree == null) || (!rusic92PropertyTree.isLeaf())))) {
      _other.rusic92 = this.rusic92;
    }
    final PropertyTree frosic2007PropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("frosic2007"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (frosic2007PropertyTree != null)
        : ((frosic2007PropertyTree == null) || (!frosic2007PropertyTree.isLeaf())))) {
      _other.frosic2007 = this.frosic2007;
    }
    final PropertyTree rusic2007PropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("rusic2007"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (rusic2007PropertyTree != null)
        : ((rusic2007PropertyTree == null) || (!rusic2007PropertyTree.isLeaf())))) {
      _other.rusic2007 = this.rusic2007;
    }
    final PropertyTree froempmentPropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("froempment"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (froempmentPropertyTree != null)
        : ((froempmentPropertyTree == null) || (!froempmentPropertyTree.isLeaf())))) {
      _other.froempment = this.froempment;
    }
    final PropertyTree frotoverPropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("frotover"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (frotoverPropertyTree != null)
        : ((frotoverPropertyTree == null) || (!frotoverPropertyTree.isLeaf())))) {
      _other.frotover = this.frotover;
    }
    final PropertyTree entrefPropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("entref"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (entrefPropertyTree != null)
        : ((entrefPropertyTree == null) || (!entrefPropertyTree.isLeaf())))) {
      _other.entref = this.entref;
    }
    final PropertyTree legalstatusPropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("legalstatus"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (legalstatusPropertyTree != null)
        : ((legalstatusPropertyTree == null) || (!legalstatusPropertyTree.isLeaf())))) {
      _other.legalstatus = this.legalstatus;
    }
    final PropertyTree namePropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("name"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (namePropertyTree != null)
        : ((namePropertyTree == null) || (!namePropertyTree.isLeaf())))) {
      _other.name = this.name;
    }
    final PropertyTree entrepmkrPropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("entrepmkr"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (entrepmkrPropertyTree != null)
        : ((entrepmkrPropertyTree == null) || (!entrepmkrPropertyTree.isLeaf())))) {
      _other.entrepmkr = this.entrepmkr;
    }
    final PropertyTree regionPropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("region"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (regionPropertyTree != null)
        : ((regionPropertyTree == null) || (!regionPropertyTree.isLeaf())))) {
      _other.region = this.region;
    }
    final PropertyTree birthdatePropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("birthdate"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (birthdatePropertyTree != null)
        : ((birthdatePropertyTree == null) || (!birthdatePropertyTree.isLeaf())))) {
      _other.birthdate = this.birthdate;
    }
    final PropertyTree entname1PropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("entname1"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (entname1PropertyTree != null)
        : ((entname1PropertyTree == null) || (!entname1PropertyTree.isLeaf())))) {
      _other.entname1 = this.entname1;
    }
    final PropertyTree entname2PropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("entname2"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (entname2PropertyTree != null)
        : ((entname2PropertyTree == null) || (!entname2PropertyTree.isLeaf())))) {
      _other.entname2 = this.entname2;
    }
    final PropertyTree entname3PropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("entname3"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (entname3PropertyTree != null)
        : ((entname3PropertyTree == null) || (!entname3PropertyTree.isLeaf())))) {
      _other.entname3 = this.entname3;
    }
    final PropertyTree runame1PropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("runame1"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (runame1PropertyTree != null)
        : ((runame1PropertyTree == null) || (!runame1PropertyTree.isLeaf())))) {
      _other.runame1 = this.runame1;
    }
    final PropertyTree runame2PropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("runame2"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (runame2PropertyTree != null)
        : ((runame2PropertyTree == null) || (!runame2PropertyTree.isLeaf())))) {
      _other.runame2 = this.runame2;
    }
    final PropertyTree runame3PropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("runame3"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (runame3PropertyTree != null)
        : ((runame3PropertyTree == null) || (!runame3PropertyTree.isLeaf())))) {
      _other.runame3 = this.runame3;
    }
    final PropertyTree tradstyle1PropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("tradstyle1"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (tradstyle1PropertyTree != null)
        : ((tradstyle1PropertyTree == null) || (!tradstyle1PropertyTree.isLeaf())))) {
      _other.tradstyle1 = this.tradstyle1;
    }
    final PropertyTree tradstyle2PropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("tradstyle2"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (tradstyle2PropertyTree != null)
        : ((tradstyle2PropertyTree == null) || (!tradstyle2PropertyTree.isLeaf())))) {
      _other.tradstyle2 = this.tradstyle2;
    }
    final PropertyTree tradstyle3PropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("tradstyle3"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (tradstyle3PropertyTree != null)
        : ((tradstyle3PropertyTree == null) || (!tradstyle3PropertyTree.isLeaf())))) {
      _other.tradstyle3 = this.tradstyle3;
    }
    final PropertyTree seltypePropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("seltype"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (seltypePropertyTree != null)
        : ((seltypePropertyTree == null) || (!seltypePropertyTree.isLeaf())))) {
      _other.seltype = this.seltype;
    }
    final PropertyTree inclexclPropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("inclexcl"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (inclexclPropertyTree != null)
        : ((inclexclPropertyTree == null) || (!inclexclPropertyTree.isLeaf())))) {
      _other.inclexcl = this.inclexcl;
    }
    final PropertyTree cellNoPropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("cellNo"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (cellNoPropertyTree != null)
        : ((cellNoPropertyTree == null) || (!cellNoPropertyTree.isLeaf())))) {
      _other.cellNo = this.cellNo;
    }
    final PropertyTree formtypePropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("formtype"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (formtypePropertyTree != null)
        : ((formtypePropertyTree == null) || (!formtypePropertyTree.isLeaf())))) {
      _other.formtype = this.formtype;
    }
    final PropertyTree currencyPropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("currency"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (currencyPropertyTree != null)
        : ((currencyPropertyTree == null) || (!currencyPropertyTree.isLeaf())))) {
      _other.currency = this.currency;
    }
    final PropertyTree sampleUnitIdPropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("sampleUnitId"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (sampleUnitIdPropertyTree != null)
        : ((sampleUnitIdPropertyTree == null) || (!sampleUnitIdPropertyTree.isLeaf())))) {
      _other.sampleUnitId = this.sampleUnitId;
    }
  }

  public <_B> PartyCreationRequestAttributesDTO.Builder<_B> newCopyBuilder(
      final _B _parentBuilder,
      final PropertyTree _propertyTree,
      final PropertyTreeUse _propertyTreeUse) {
    return new PartyCreationRequestAttributesDTO.Builder<_B>(
        _parentBuilder, this, true, _propertyTree, _propertyTreeUse);
  }

  public PartyCreationRequestAttributesDTO.Builder<Void> newCopyBuilder(
      final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
    return newCopyBuilder(null, _propertyTree, _propertyTreeUse);
  }

  public static <_B> PartyCreationRequestAttributesDTO.Builder<_B> copyOf(
      final PartyCreationRequestAttributesDTO _other,
      final PropertyTree _propertyTree,
      final PropertyTreeUse _propertyTreeUse) {
    final PartyCreationRequestAttributesDTO.Builder<_B> _newBuilder =
        new PartyCreationRequestAttributesDTO.Builder<_B>(null, null, false);
    _other.copyTo(_newBuilder, _propertyTree, _propertyTreeUse);
    return _newBuilder;
  }

  public static PartyCreationRequestAttributesDTO.Builder<Void> copyExcept(
      final PartyCreationRequestAttributesDTO _other, final PropertyTree _propertyTree) {
    return copyOf(_other, _propertyTree, PropertyTreeUse.EXCLUDE);
  }

  public static PartyCreationRequestAttributesDTO.Builder<Void> copyOnly(
      final PartyCreationRequestAttributesDTO _other, final PropertyTree _propertyTree) {
    return copyOf(_other, _propertyTree, PropertyTreeUse.INCLUDE);
  }

  public static class Builder<_B> implements Buildable {

    protected final _B _parentBuilder;
    protected final PartyCreationRequestAttributesDTO _storedValue;
    private String checkletter;
    private String frosic92;
    private String rusic92;
    private String frosic2007;
    private String rusic2007;
    private int froempment;
    private int frotover;
    private String entref;
    private String legalstatus;
    private String name;
    private String entrepmkr;
    private String region;
    private String birthdate;
    private String entname1;
    private String entname2;
    private String entname3;
    private String runame1;
    private String runame2;
    private String runame3;
    private String tradstyle1;
    private String tradstyle2;
    private String tradstyle3;
    private String seltype;
    private String inclexcl;
    private int cellNo;
    private String formtype;
    private String currency;
    private String sampleUnitId;

    public Builder(
        final _B _parentBuilder,
        final PartyCreationRequestAttributesDTO _other,
        final boolean _copy) {
      this._parentBuilder = _parentBuilder;
      if (_other != null) {
        if (_copy) {
          _storedValue = null;
          this.checkletter = _other.checkletter;
          this.frosic92 = _other.frosic92;
          this.rusic92 = _other.rusic92;
          this.frosic2007 = _other.frosic2007;
          this.rusic2007 = _other.rusic2007;
          this.froempment = _other.froempment;
          this.frotover = _other.frotover;
          this.entref = _other.entref;
          this.legalstatus = _other.legalstatus;
          this.name = _other.name;
          this.entrepmkr = _other.entrepmkr;
          this.region = _other.region;
          this.birthdate = _other.birthdate;
          this.entname1 = _other.entname1;
          this.entname2 = _other.entname2;
          this.entname3 = _other.entname3;
          this.runame1 = _other.runame1;
          this.runame2 = _other.runame2;
          this.runame3 = _other.runame3;
          this.tradstyle1 = _other.tradstyle1;
          this.tradstyle2 = _other.tradstyle2;
          this.tradstyle3 = _other.tradstyle3;
          this.seltype = _other.seltype;
          this.inclexcl = _other.inclexcl;
          this.cellNo = _other.cellNo;
          this.formtype = _other.formtype;
          this.currency = _other.currency;
          this.sampleUnitId = _other.sampleUnitId;
        } else {
          _storedValue = _other;
        }
      } else {
        _storedValue = null;
      }
    }

    public Builder(
        final _B _parentBuilder,
        final PartyCreationRequestAttributesDTO _other,
        final boolean _copy,
        final PropertyTree _propertyTree,
        final PropertyTreeUse _propertyTreeUse) {
      this._parentBuilder = _parentBuilder;
      if (_other != null) {
        if (_copy) {
          _storedValue = null;
          final PropertyTree checkletterPropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("checkletter"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (checkletterPropertyTree != null)
              : ((checkletterPropertyTree == null) || (!checkletterPropertyTree.isLeaf())))) {
            this.checkletter = _other.checkletter;
          }
          final PropertyTree frosic92PropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("frosic92"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (frosic92PropertyTree != null)
              : ((frosic92PropertyTree == null) || (!frosic92PropertyTree.isLeaf())))) {
            this.frosic92 = _other.frosic92;
          }
          final PropertyTree rusic92PropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("rusic92"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (rusic92PropertyTree != null)
              : ((rusic92PropertyTree == null) || (!rusic92PropertyTree.isLeaf())))) {
            this.rusic92 = _other.rusic92;
          }
          final PropertyTree frosic2007PropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("frosic2007"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (frosic2007PropertyTree != null)
              : ((frosic2007PropertyTree == null) || (!frosic2007PropertyTree.isLeaf())))) {
            this.frosic2007 = _other.frosic2007;
          }
          final PropertyTree rusic2007PropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("rusic2007"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (rusic2007PropertyTree != null)
              : ((rusic2007PropertyTree == null) || (!rusic2007PropertyTree.isLeaf())))) {
            this.rusic2007 = _other.rusic2007;
          }
          final PropertyTree froempmentPropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("froempment"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (froempmentPropertyTree != null)
              : ((froempmentPropertyTree == null) || (!froempmentPropertyTree.isLeaf())))) {
            this.froempment = _other.froempment;
          }
          final PropertyTree frotoverPropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("frotover"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (frotoverPropertyTree != null)
              : ((frotoverPropertyTree == null) || (!frotoverPropertyTree.isLeaf())))) {
            this.frotover = _other.frotover;
          }
          final PropertyTree entrefPropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("entref"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (entrefPropertyTree != null)
              : ((entrefPropertyTree == null) || (!entrefPropertyTree.isLeaf())))) {
            this.entref = _other.entref;
          }
          final PropertyTree legalstatusPropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("legalstatus"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (legalstatusPropertyTree != null)
              : ((legalstatusPropertyTree == null) || (!legalstatusPropertyTree.isLeaf())))) {
            this.legalstatus = _other.legalstatus;
          }
          final PropertyTree namePropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("name"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (namePropertyTree != null)
              : ((namePropertyTree == null) || (!namePropertyTree.isLeaf())))) {
            this.name = _other.name;
          }
          final PropertyTree entrepmkrPropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("entrepmkr"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (entrepmkrPropertyTree != null)
              : ((entrepmkrPropertyTree == null) || (!entrepmkrPropertyTree.isLeaf())))) {
            this.entrepmkr = _other.entrepmkr;
          }
          final PropertyTree regionPropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("region"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (regionPropertyTree != null)
              : ((regionPropertyTree == null) || (!regionPropertyTree.isLeaf())))) {
            this.region = _other.region;
          }
          final PropertyTree birthdatePropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("birthdate"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (birthdatePropertyTree != null)
              : ((birthdatePropertyTree == null) || (!birthdatePropertyTree.isLeaf())))) {
            this.birthdate = _other.birthdate;
          }
          final PropertyTree entname1PropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("entname1"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (entname1PropertyTree != null)
              : ((entname1PropertyTree == null) || (!entname1PropertyTree.isLeaf())))) {
            this.entname1 = _other.entname1;
          }
          final PropertyTree entname2PropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("entname2"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (entname2PropertyTree != null)
              : ((entname2PropertyTree == null) || (!entname2PropertyTree.isLeaf())))) {
            this.entname2 = _other.entname2;
          }
          final PropertyTree entname3PropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("entname3"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (entname3PropertyTree != null)
              : ((entname3PropertyTree == null) || (!entname3PropertyTree.isLeaf())))) {
            this.entname3 = _other.entname3;
          }
          final PropertyTree runame1PropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("runame1"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (runame1PropertyTree != null)
              : ((runame1PropertyTree == null) || (!runame1PropertyTree.isLeaf())))) {
            this.runame1 = _other.runame1;
          }
          final PropertyTree runame2PropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("runame2"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (runame2PropertyTree != null)
              : ((runame2PropertyTree == null) || (!runame2PropertyTree.isLeaf())))) {
            this.runame2 = _other.runame2;
          }
          final PropertyTree runame3PropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("runame3"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (runame3PropertyTree != null)
              : ((runame3PropertyTree == null) || (!runame3PropertyTree.isLeaf())))) {
            this.runame3 = _other.runame3;
          }
          final PropertyTree tradstyle1PropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("tradstyle1"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (tradstyle1PropertyTree != null)
              : ((tradstyle1PropertyTree == null) || (!tradstyle1PropertyTree.isLeaf())))) {
            this.tradstyle1 = _other.tradstyle1;
          }
          final PropertyTree tradstyle2PropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("tradstyle2"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (tradstyle2PropertyTree != null)
              : ((tradstyle2PropertyTree == null) || (!tradstyle2PropertyTree.isLeaf())))) {
            this.tradstyle2 = _other.tradstyle2;
          }
          final PropertyTree tradstyle3PropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("tradstyle3"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (tradstyle3PropertyTree != null)
              : ((tradstyle3PropertyTree == null) || (!tradstyle3PropertyTree.isLeaf())))) {
            this.tradstyle3 = _other.tradstyle3;
          }
          final PropertyTree seltypePropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("seltype"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (seltypePropertyTree != null)
              : ((seltypePropertyTree == null) || (!seltypePropertyTree.isLeaf())))) {
            this.seltype = _other.seltype;
          }
          final PropertyTree inclexclPropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("inclexcl"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (inclexclPropertyTree != null)
              : ((inclexclPropertyTree == null) || (!inclexclPropertyTree.isLeaf())))) {
            this.inclexcl = _other.inclexcl;
          }
          final PropertyTree cellNoPropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("cellNo"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (cellNoPropertyTree != null)
              : ((cellNoPropertyTree == null) || (!cellNoPropertyTree.isLeaf())))) {
            this.cellNo = _other.cellNo;
          }
          final PropertyTree formtypePropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("formtype"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (formtypePropertyTree != null)
              : ((formtypePropertyTree == null) || (!formtypePropertyTree.isLeaf())))) {
            this.formtype = _other.formtype;
          }
          final PropertyTree currencyPropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("currency"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (currencyPropertyTree != null)
              : ((currencyPropertyTree == null) || (!currencyPropertyTree.isLeaf())))) {
            this.currency = _other.currency;
          }
          final PropertyTree sampleUnitIdPropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("sampleUnitId"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (sampleUnitIdPropertyTree != null)
              : ((sampleUnitIdPropertyTree == null) || (!sampleUnitIdPropertyTree.isLeaf())))) {
            this.sampleUnitId = _other.sampleUnitId;
          }
        } else {
          _storedValue = _other;
        }
      } else {
        _storedValue = null;
      }
    }

    public _B end() {
      return this._parentBuilder;
    }

    protected <_P extends PartyCreationRequestAttributesDTO> _P init(final _P _product) {
      _product.checkletter = this.checkletter;
      _product.frosic92 = this.frosic92;
      _product.rusic92 = this.rusic92;
      _product.frosic2007 = this.frosic2007;
      _product.rusic2007 = this.rusic2007;
      _product.froempment = this.froempment;
      _product.frotover = this.frotover;
      _product.entref = this.entref;
      _product.legalstatus = this.legalstatus;
      _product.name = this.name;
      _product.entrepmkr = this.entrepmkr;
      _product.region = this.region;
      _product.birthdate = this.birthdate;
      _product.entname1 = this.entname1;
      _product.entname2 = this.entname2;
      _product.entname3 = this.entname3;
      _product.runame1 = this.runame1;
      _product.runame2 = this.runame2;
      _product.runame3 = this.runame3;
      _product.tradstyle1 = this.tradstyle1;
      _product.tradstyle2 = this.tradstyle2;
      _product.tradstyle3 = this.tradstyle3;
      _product.seltype = this.seltype;
      _product.inclexcl = this.inclexcl;
      _product.cellNo = this.cellNo;
      _product.formtype = this.formtype;
      _product.currency = this.currency;
      _product.sampleUnitId = this.sampleUnitId;
      return _product;
    }

    /**
     * Sets the new value of "checkletter" (any previous value will be replaced)
     *
     * @param checkletter New value of the "checkletter" property.
     */
    public PartyCreationRequestAttributesDTO.Builder<_B> withCheckletter(final String checkletter) {
      this.checkletter = checkletter;
      return this;
    }

    /**
     * Sets the new value of "frosic92" (any previous value will be replaced)
     *
     * @param frosic92 New value of the "frosic92" property.
     */
    public PartyCreationRequestAttributesDTO.Builder<_B> withFrosic92(final String frosic92) {
      this.frosic92 = frosic92;
      return this;
    }

    /**
     * Sets the new value of "rusic92" (any previous value will be replaced)
     *
     * @param rusic92 New value of the "rusic92" property.
     */
    public PartyCreationRequestAttributesDTO.Builder<_B> withRusic92(final String rusic92) {
      this.rusic92 = rusic92;
      return this;
    }

    /**
     * Sets the new value of "frosic2007" (any previous value will be replaced)
     *
     * @param frosic2007 New value of the "frosic2007" property.
     */
    public PartyCreationRequestAttributesDTO.Builder<_B> withFrosic2007(final String frosic2007) {
      this.frosic2007 = frosic2007;
      return this;
    }

    /**
     * Sets the new value of "rusic2007" (any previous value will be replaced)
     *
     * @param rusic2007 New value of the "rusic2007" property.
     */
    public PartyCreationRequestAttributesDTO.Builder<_B> withRusic2007(final String rusic2007) {
      this.rusic2007 = rusic2007;
      return this;
    }

    /**
     * Sets the new value of "froempment" (any previous value will be replaced)
     *
     * @param froempment New value of the "froempment" property.
     */
    public PartyCreationRequestAttributesDTO.Builder<_B> withFroempment(final int froempment) {
      this.froempment = froempment;
      return this;
    }

    /**
     * Sets the new value of "frotover" (any previous value will be replaced)
     *
     * @param frotover New value of the "frotover" property.
     */
    public PartyCreationRequestAttributesDTO.Builder<_B> withFrotover(final int frotover) {
      this.frotover = frotover;
      return this;
    }

    /**
     * Sets the new value of "entref" (any previous value will be replaced)
     *
     * @param entref New value of the "entref" property.
     */
    public PartyCreationRequestAttributesDTO.Builder<_B> withEntref(final String entref) {
      this.entref = entref;
      return this;
    }

    /**
     * Sets the new value of "legalstatus" (any previous value will be replaced)
     *
     * @param legalstatus New value of the "legalstatus" property.
     */
    public PartyCreationRequestAttributesDTO.Builder<_B> withLegalstatus(final String legalstatus) {
      this.legalstatus = legalstatus;
      return this;
    }

    /**
     * Sets the new value of "name" (any previous value will be replaced)
     *
     * @param name New value of the "name" property.
     */
    public PartyCreationRequestAttributesDTO.Builder<_B> withName(final String name) {
      this.name = name;
      return this;
    }

    /**
     * Sets the new value of "entrepmkr" (any previous value will be replaced)
     *
     * @param entrepmkr New value of the "entrepmkr" property.
     */
    public PartyCreationRequestAttributesDTO.Builder<_B> withEntrepmkr(final String entrepmkr) {
      this.entrepmkr = entrepmkr;
      return this;
    }

    /**
     * Sets the new value of "region" (any previous value will be replaced)
     *
     * @param region New value of the "region" property.
     */
    public PartyCreationRequestAttributesDTO.Builder<_B> withRegion(final String region) {
      this.region = region;
      return this;
    }

    /**
     * Sets the new value of "birthdate" (any previous value will be replaced)
     *
     * @param birthdate New value of the "birthdate" property.
     */
    public PartyCreationRequestAttributesDTO.Builder<_B> withBirthdate(final String birthdate) {
      this.birthdate = birthdate;
      return this;
    }

    /**
     * Sets the new value of "entname1" (any previous value will be replaced)
     *
     * @param entname1 New value of the "entname1" property.
     */
    public PartyCreationRequestAttributesDTO.Builder<_B> withEntname1(final String entname1) {
      this.entname1 = entname1;
      return this;
    }

    /**
     * Sets the new value of "entname2" (any previous value will be replaced)
     *
     * @param entname2 New value of the "entname2" property.
     */
    public PartyCreationRequestAttributesDTO.Builder<_B> withEntname2(final String entname2) {
      this.entname2 = entname2;
      return this;
    }

    /**
     * Sets the new value of "entname3" (any previous value will be replaced)
     *
     * @param entname3 New value of the "entname3" property.
     */
    public PartyCreationRequestAttributesDTO.Builder<_B> withEntname3(final String entname3) {
      this.entname3 = entname3;
      return this;
    }

    /**
     * Sets the new value of "runame1" (any previous value will be replaced)
     *
     * @param runame1 New value of the "runame1" property.
     */
    public PartyCreationRequestAttributesDTO.Builder<_B> withRuname1(final String runame1) {
      this.runame1 = runame1;
      return this;
    }

    /**
     * Sets the new value of "runame2" (any previous value will be replaced)
     *
     * @param runame2 New value of the "runame2" property.
     */
    public PartyCreationRequestAttributesDTO.Builder<_B> withRuname2(final String runame2) {
      this.runame2 = runame2;
      return this;
    }

    /**
     * Sets the new value of "runame3" (any previous value will be replaced)
     *
     * @param runame3 New value of the "runame3" property.
     */
    public PartyCreationRequestAttributesDTO.Builder<_B> withRuname3(final String runame3) {
      this.runame3 = runame3;
      return this;
    }

    /**
     * Sets the new value of "tradstyle1" (any previous value will be replaced)
     *
     * @param tradstyle1 New value of the "tradstyle1" property.
     */
    public PartyCreationRequestAttributesDTO.Builder<_B> withTradstyle1(final String tradstyle1) {
      this.tradstyle1 = tradstyle1;
      return this;
    }

    /**
     * Sets the new value of "tradstyle2" (any previous value will be replaced)
     *
     * @param tradstyle2 New value of the "tradstyle2" property.
     */
    public PartyCreationRequestAttributesDTO.Builder<_B> withTradstyle2(final String tradstyle2) {
      this.tradstyle2 = tradstyle2;
      return this;
    }

    /**
     * Sets the new value of "tradstyle3" (any previous value will be replaced)
     *
     * @param tradstyle3 New value of the "tradstyle3" property.
     */
    public PartyCreationRequestAttributesDTO.Builder<_B> withTradstyle3(final String tradstyle3) {
      this.tradstyle3 = tradstyle3;
      return this;
    }

    /**
     * Sets the new value of "seltype" (any previous value will be replaced)
     *
     * @param seltype New value of the "seltype" property.
     */
    public PartyCreationRequestAttributesDTO.Builder<_B> withSeltype(final String seltype) {
      this.seltype = seltype;
      return this;
    }

    /**
     * Sets the new value of "inclexcl" (any previous value will be replaced)
     *
     * @param inclexcl New value of the "inclexcl" property.
     */
    public PartyCreationRequestAttributesDTO.Builder<_B> withInclexcl(final String inclexcl) {
      this.inclexcl = inclexcl;
      return this;
    }

    /**
     * Sets the new value of "cellNo" (any previous value will be replaced)
     *
     * @param cellNo New value of the "cellNo" property.
     */
    public PartyCreationRequestAttributesDTO.Builder<_B> withCellNo(final int cellNo) {
      this.cellNo = cellNo;
      return this;
    }

    /**
     * Sets the new value of "formtype" (any previous value will be replaced)
     *
     * @param formtype New value of the "formtype" property.
     */
    public PartyCreationRequestAttributesDTO.Builder<_B> withFormtype(final String formtype) {
      this.formtype = formtype;
      return this;
    }

    /**
     * Sets the new value of "currency" (any previous value will be replaced)
     *
     * @param currency New value of the "currency" property.
     */
    public PartyCreationRequestAttributesDTO.Builder<_B> withCurrency(final String currency) {
      this.currency = currency;
      return this;
    }

    /**
     * Sets the new value of "sampleUnitId" (any previous value will be replaced)
     *
     * @param sampleUnitId New value of the "sampleUnitId" property.
     */
    public PartyCreationRequestAttributesDTO.Builder<_B> withSampleUnitId(
        final String sampleUnitId) {
      this.sampleUnitId = sampleUnitId;
      return this;
    }

    @Override
    public PartyCreationRequestAttributesDTO build() {
      if (_storedValue == null) {
        return this.init(new PartyCreationRequestAttributesDTO());
      } else {
        return ((PartyCreationRequestAttributesDTO) _storedValue);
      }
    }
  }

  public static class Select
      extends PartyCreationRequestAttributesDTO.Selector<
          PartyCreationRequestAttributesDTO.Select, Void> {

    Select() {
      super(null, null, null);
    }

    public static PartyCreationRequestAttributesDTO.Select _root() {
      return new PartyCreationRequestAttributesDTO.Select();
    }
  }

  public static class Selector<TRoot extends com.kscs.util.jaxb.Selector<TRoot, ?>, TParent>
      extends com.kscs.util.jaxb.Selector<TRoot, TParent> {

    private com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        checkletter = null;
    private com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        frosic92 = null;
    private com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        rusic92 = null;
    private com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        frosic2007 = null;
    private com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        rusic2007 = null;
    private com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        entref = null;
    private com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        legalstatus = null;
    private com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        name = null;
    private com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        entrepmkr = null;
    private com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        region = null;
    private com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        birthdate = null;
    private com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        entname1 = null;
    private com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        entname2 = null;
    private com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        entname3 = null;
    private com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        runame1 = null;
    private com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        runame2 = null;
    private com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        runame3 = null;
    private com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        tradstyle1 = null;
    private com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        tradstyle2 = null;
    private com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        tradstyle3 = null;
    private com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        seltype = null;
    private com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        inclexcl = null;
    private com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        formtype = null;
    private com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        currency = null;
    private com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        sampleUnitId = null;

    public Selector(final TRoot root, final TParent parent, final String propertyName) {
      super(root, parent, propertyName);
    }

    @Override
    public Map<String, PropertyTree> buildChildren() {
      final Map<String, PropertyTree> products = new HashMap<String, PropertyTree>();
      products.putAll(super.buildChildren());
      if (this.checkletter != null) {
        products.put("checkletter", this.checkletter.init());
      }
      if (this.frosic92 != null) {
        products.put("frosic92", this.frosic92.init());
      }
      if (this.rusic92 != null) {
        products.put("rusic92", this.rusic92.init());
      }
      if (this.frosic2007 != null) {
        products.put("frosic2007", this.frosic2007.init());
      }
      if (this.rusic2007 != null) {
        products.put("rusic2007", this.rusic2007.init());
      }
      if (this.entref != null) {
        products.put("entref", this.entref.init());
      }
      if (this.legalstatus != null) {
        products.put("legalstatus", this.legalstatus.init());
      }
      if (this.name != null) {
        products.put("name", this.name.init());
      }
      if (this.entrepmkr != null) {
        products.put("entrepmkr", this.entrepmkr.init());
      }
      if (this.region != null) {
        products.put("region", this.region.init());
      }
      if (this.birthdate != null) {
        products.put("birthdate", this.birthdate.init());
      }
      if (this.entname1 != null) {
        products.put("entname1", this.entname1.init());
      }
      if (this.entname2 != null) {
        products.put("entname2", this.entname2.init());
      }
      if (this.entname3 != null) {
        products.put("entname3", this.entname3.init());
      }
      if (this.runame1 != null) {
        products.put("runame1", this.runame1.init());
      }
      if (this.runame2 != null) {
        products.put("runame2", this.runame2.init());
      }
      if (this.runame3 != null) {
        products.put("runame3", this.runame3.init());
      }
      if (this.tradstyle1 != null) {
        products.put("tradstyle1", this.tradstyle1.init());
      }
      if (this.tradstyle2 != null) {
        products.put("tradstyle2", this.tradstyle2.init());
      }
      if (this.tradstyle3 != null) {
        products.put("tradstyle3", this.tradstyle3.init());
      }
      if (this.seltype != null) {
        products.put("seltype", this.seltype.init());
      }
      if (this.inclexcl != null) {
        products.put("inclexcl", this.inclexcl.init());
      }
      if (this.formtype != null) {
        products.put("formtype", this.formtype.init());
      }
      if (this.currency != null) {
        products.put("currency", this.currency.init());
      }
      if (this.sampleUnitId != null) {
        products.put("sampleUnitId", this.sampleUnitId.init());
      }
      return products;
    }

    public com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        checkletter() {
      return ((this.checkletter == null)
          ? this.checkletter =
              new com.kscs.util.jaxb.Selector<
                  TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>(
                  this._root, this, "checkletter")
          : this.checkletter);
    }

    public com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        frosic92() {
      return ((this.frosic92 == null)
          ? this.frosic92 =
              new com.kscs.util.jaxb.Selector<
                  TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>(
                  this._root, this, "frosic92")
          : this.frosic92);
    }

    public com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        rusic92() {
      return ((this.rusic92 == null)
          ? this.rusic92 =
              new com.kscs.util.jaxb.Selector<
                  TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>(
                  this._root, this, "rusic92")
          : this.rusic92);
    }

    public com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        frosic2007() {
      return ((this.frosic2007 == null)
          ? this.frosic2007 =
              new com.kscs.util.jaxb.Selector<
                  TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>(
                  this._root, this, "frosic2007")
          : this.frosic2007);
    }

    public com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        rusic2007() {
      return ((this.rusic2007 == null)
          ? this.rusic2007 =
              new com.kscs.util.jaxb.Selector<
                  TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>(
                  this._root, this, "rusic2007")
          : this.rusic2007);
    }

    public com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        entref() {
      return ((this.entref == null)
          ? this.entref =
              new com.kscs.util.jaxb.Selector<
                  TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>(
                  this._root, this, "entref")
          : this.entref);
    }

    public com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        legalstatus() {
      return ((this.legalstatus == null)
          ? this.legalstatus =
              new com.kscs.util.jaxb.Selector<
                  TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>(
                  this._root, this, "legalstatus")
          : this.legalstatus);
    }

    public com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        name() {
      return ((this.name == null)
          ? this.name =
              new com.kscs.util.jaxb.Selector<
                  TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>(
                  this._root, this, "name")
          : this.name);
    }

    public com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        entrepmkr() {
      return ((this.entrepmkr == null)
          ? this.entrepmkr =
              new com.kscs.util.jaxb.Selector<
                  TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>(
                  this._root, this, "entrepmkr")
          : this.entrepmkr);
    }

    public com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        region() {
      return ((this.region == null)
          ? this.region =
              new com.kscs.util.jaxb.Selector<
                  TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>(
                  this._root, this, "region")
          : this.region);
    }

    public com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        birthdate() {
      return ((this.birthdate == null)
          ? this.birthdate =
              new com.kscs.util.jaxb.Selector<
                  TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>(
                  this._root, this, "birthdate")
          : this.birthdate);
    }

    public com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        entname1() {
      return ((this.entname1 == null)
          ? this.entname1 =
              new com.kscs.util.jaxb.Selector<
                  TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>(
                  this._root, this, "entname1")
          : this.entname1);
    }

    public com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        entname2() {
      return ((this.entname2 == null)
          ? this.entname2 =
              new com.kscs.util.jaxb.Selector<
                  TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>(
                  this._root, this, "entname2")
          : this.entname2);
    }

    public com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        entname3() {
      return ((this.entname3 == null)
          ? this.entname3 =
              new com.kscs.util.jaxb.Selector<
                  TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>(
                  this._root, this, "entname3")
          : this.entname3);
    }

    public com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        runame1() {
      return ((this.runame1 == null)
          ? this.runame1 =
              new com.kscs.util.jaxb.Selector<
                  TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>(
                  this._root, this, "runame1")
          : this.runame1);
    }

    public com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        runame2() {
      return ((this.runame2 == null)
          ? this.runame2 =
              new com.kscs.util.jaxb.Selector<
                  TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>(
                  this._root, this, "runame2")
          : this.runame2);
    }

    public com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        runame3() {
      return ((this.runame3 == null)
          ? this.runame3 =
              new com.kscs.util.jaxb.Selector<
                  TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>(
                  this._root, this, "runame3")
          : this.runame3);
    }

    public com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        tradstyle1() {
      return ((this.tradstyle1 == null)
          ? this.tradstyle1 =
              new com.kscs.util.jaxb.Selector<
                  TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>(
                  this._root, this, "tradstyle1")
          : this.tradstyle1);
    }

    public com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        tradstyle2() {
      return ((this.tradstyle2 == null)
          ? this.tradstyle2 =
              new com.kscs.util.jaxb.Selector<
                  TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>(
                  this._root, this, "tradstyle2")
          : this.tradstyle2);
    }

    public com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        tradstyle3() {
      return ((this.tradstyle3 == null)
          ? this.tradstyle3 =
              new com.kscs.util.jaxb.Selector<
                  TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>(
                  this._root, this, "tradstyle3")
          : this.tradstyle3);
    }

    public com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        seltype() {
      return ((this.seltype == null)
          ? this.seltype =
              new com.kscs.util.jaxb.Selector<
                  TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>(
                  this._root, this, "seltype")
          : this.seltype);
    }

    public com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        inclexcl() {
      return ((this.inclexcl == null)
          ? this.inclexcl =
              new com.kscs.util.jaxb.Selector<
                  TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>(
                  this._root, this, "inclexcl")
          : this.inclexcl);
    }

    public com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        formtype() {
      return ((this.formtype == null)
          ? this.formtype =
              new com.kscs.util.jaxb.Selector<
                  TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>(
                  this._root, this, "formtype")
          : this.formtype);
    }

    public com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        currency() {
      return ((this.currency == null)
          ? this.currency =
              new com.kscs.util.jaxb.Selector<
                  TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>(
                  this._root, this, "currency")
          : this.currency);
    }

    public com.kscs.util.jaxb.Selector<
            TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>
        sampleUnitId() {
      return ((this.sampleUnitId == null)
          ? this.sampleUnitId =
              new com.kscs.util.jaxb.Selector<
                  TRoot, PartyCreationRequestAttributesDTO.Selector<TRoot, TParent>>(
                  this._root, this, "sampleUnitId")
          : this.sampleUnitId);
    }
  }
}
