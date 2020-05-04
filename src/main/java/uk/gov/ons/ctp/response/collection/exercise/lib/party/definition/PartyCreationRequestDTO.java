package uk.gov.ons.ctp.response.collection.exercise.lib.party.definition;

import com.kscs.util.jaxb.Buildable;
import com.kscs.util.jaxb.PropertyTree;
import com.kscs.util.jaxb.PropertyTreeUse;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.*;

/**
 * Java class for PartyCreationRequestDTO complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="PartyCreationRequestDTO"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="sampleUnitRef" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="sampleSummaryId" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="sampleUnitType"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *               &lt;enumeration value="H"/&gt;
 *               &lt;enumeration value="HI"/&gt;
 *               &lt;enumeration value="C"/&gt;
 *               &lt;enumeration value="CI"/&gt;
 *               &lt;enumeration value="B"/&gt;
 *               &lt;enumeration value="BI"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="attributes" type="{http://ons.gov.uk/ctp/response/party/definition}PartyCreationRequestAttributesDTO"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "PartyCreationRequestDTO",
    propOrder = {"sampleUnitRef", "sampleSummaryId", "sampleUnitType", "attributes"})
@XmlRootElement(name = "partyCreationRequestDTO")
public class PartyCreationRequestDTO {

  @XmlElement(required = true)
  protected String sampleUnitRef;

  @XmlElement(required = true)
  protected String sampleSummaryId;

  @XmlElement(required = true)
  protected String sampleUnitType;

  @XmlElement(required = true)
  protected PartyCreationRequestAttributesDTO attributes;

  /** Default no-arg constructor */
  public PartyCreationRequestDTO() {
    super();
  }

  /** Fully-initialising value constructor */
  public PartyCreationRequestDTO(
      final String sampleUnitRef,
      final String sampleSummaryId,
      final String sampleUnitType,
      final PartyCreationRequestAttributesDTO attributes) {
    this.sampleUnitRef = sampleUnitRef;
    this.sampleSummaryId = sampleSummaryId;
    this.sampleUnitType = sampleUnitType;
    this.attributes = attributes;
  }

  /**
   * Gets the value of the sampleUnitRef property.
   *
   * @return possible object is {@link String }
   */
  public String getSampleUnitRef() {
    return sampleUnitRef;
  }

  /**
   * Sets the value of the sampleUnitRef property.
   *
   * @param value allowed object is {@link String }
   */
  public void setSampleUnitRef(String value) {
    this.sampleUnitRef = value;
  }

  /**
   * Gets the value of the sampleSummaryId property.
   *
   * @return possible object is {@link String }
   */
  public String getSampleSummaryId() {
    return sampleSummaryId;
  }

  /**
   * Sets the value of the sampleSummaryId property.
   *
   * @param value allowed object is {@link String }
   */
  public void setSampleSummaryId(String value) {
    this.sampleSummaryId = value;
  }

  /**
   * Gets the value of the sampleUnitType property.
   *
   * @return possible object is {@link String }
   */
  public String getSampleUnitType() {
    return sampleUnitType;
  }

  /**
   * Sets the value of the sampleUnitType property.
   *
   * @param value allowed object is {@link String }
   */
  public void setSampleUnitType(String value) {
    this.sampleUnitType = value;
  }

  /**
   * Gets the value of the attributes property.
   *
   * @return possible object is {@link PartyCreationRequestAttributesDTO }
   */
  public PartyCreationRequestAttributesDTO getAttributes() {
    return attributes;
  }

  /**
   * Sets the value of the attributes property.
   *
   * @param value allowed object is {@link PartyCreationRequestAttributesDTO }
   */
  public void setAttributes(PartyCreationRequestAttributesDTO value) {
    this.attributes = value;
  }

  /**
   * Copies all state of this object to a builder. This method is used by the {@link #copyOf} method
   * and should not be called directly by client code.
   *
   * @param _other A builder instance to which the state of this object will be copied.
   */
  public <_B> void copyTo(final PartyCreationRequestDTO.Builder<_B> _other) {
    _other.sampleUnitRef = this.sampleUnitRef;
    _other.sampleSummaryId = this.sampleSummaryId;
    _other.sampleUnitType = this.sampleUnitType;
    _other.attributes = ((this.attributes == null) ? null : this.attributes.newCopyBuilder(_other));
  }

  public <_B> PartyCreationRequestDTO.Builder<_B> newCopyBuilder(final _B _parentBuilder) {
    return new PartyCreationRequestDTO.Builder<_B>(_parentBuilder, this, true);
  }

  public PartyCreationRequestDTO.Builder<Void> newCopyBuilder() {
    return newCopyBuilder(null);
  }

  public static PartyCreationRequestDTO.Builder<Void> builder() {
    return new PartyCreationRequestDTO.Builder<Void>(null, null, false);
  }

  public static <_B> PartyCreationRequestDTO.Builder<_B> copyOf(
      final PartyCreationRequestDTO _other) {
    final PartyCreationRequestDTO.Builder<_B> _newBuilder =
        new PartyCreationRequestDTO.Builder<_B>(null, null, false);
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
      final PartyCreationRequestDTO.Builder<_B> _other,
      final PropertyTree _propertyTree,
      final PropertyTreeUse _propertyTreeUse) {
    final PropertyTree sampleUnitRefPropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("sampleUnitRef"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (sampleUnitRefPropertyTree != null)
        : ((sampleUnitRefPropertyTree == null) || (!sampleUnitRefPropertyTree.isLeaf())))) {
      _other.sampleUnitRef = this.sampleUnitRef;
    }
    final PropertyTree sampleSummaryIdPropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("sampleSummaryId"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (sampleSummaryIdPropertyTree != null)
        : ((sampleSummaryIdPropertyTree == null) || (!sampleSummaryIdPropertyTree.isLeaf())))) {
      _other.sampleSummaryId = this.sampleSummaryId;
    }
    final PropertyTree sampleUnitTypePropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("sampleUnitType"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (sampleUnitTypePropertyTree != null)
        : ((sampleUnitTypePropertyTree == null) || (!sampleUnitTypePropertyTree.isLeaf())))) {
      _other.sampleUnitType = this.sampleUnitType;
    }
    final PropertyTree attributesPropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("attributes"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (attributesPropertyTree != null)
        : ((attributesPropertyTree == null) || (!attributesPropertyTree.isLeaf())))) {
      _other.attributes =
          ((this.attributes == null)
              ? null
              : this.attributes.newCopyBuilder(_other, attributesPropertyTree, _propertyTreeUse));
    }
  }

  public <_B> PartyCreationRequestDTO.Builder<_B> newCopyBuilder(
      final _B _parentBuilder,
      final PropertyTree _propertyTree,
      final PropertyTreeUse _propertyTreeUse) {
    return new PartyCreationRequestDTO.Builder<_B>(
        _parentBuilder, this, true, _propertyTree, _propertyTreeUse);
  }

  public PartyCreationRequestDTO.Builder<Void> newCopyBuilder(
      final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
    return newCopyBuilder(null, _propertyTree, _propertyTreeUse);
  }

  public static <_B> PartyCreationRequestDTO.Builder<_B> copyOf(
      final PartyCreationRequestDTO _other,
      final PropertyTree _propertyTree,
      final PropertyTreeUse _propertyTreeUse) {
    final PartyCreationRequestDTO.Builder<_B> _newBuilder =
        new PartyCreationRequestDTO.Builder<_B>(null, null, false);
    _other.copyTo(_newBuilder, _propertyTree, _propertyTreeUse);
    return _newBuilder;
  }

  public static PartyCreationRequestDTO.Builder<Void> copyExcept(
      final PartyCreationRequestDTO _other, final PropertyTree _propertyTree) {
    return copyOf(_other, _propertyTree, PropertyTreeUse.EXCLUDE);
  }

  public static PartyCreationRequestDTO.Builder<Void> copyOnly(
      final PartyCreationRequestDTO _other, final PropertyTree _propertyTree) {
    return copyOf(_other, _propertyTree, PropertyTreeUse.INCLUDE);
  }

  public static class Builder<_B> implements Buildable {

    protected final _B _parentBuilder;
    protected final PartyCreationRequestDTO _storedValue;
    private String sampleUnitRef;
    private String sampleSummaryId;
    private String sampleUnitType;
    private PartyCreationRequestAttributesDTO.Builder<PartyCreationRequestDTO.Builder<_B>>
        attributes;

    public Builder(
        final _B _parentBuilder, final PartyCreationRequestDTO _other, final boolean _copy) {
      this._parentBuilder = _parentBuilder;
      if (_other != null) {
        if (_copy) {
          _storedValue = null;
          this.sampleUnitRef = _other.sampleUnitRef;
          this.sampleSummaryId = _other.sampleSummaryId;
          this.sampleUnitType = _other.sampleUnitType;
          this.attributes =
              ((_other.attributes == null) ? null : _other.attributes.newCopyBuilder(this));
        } else {
          _storedValue = _other;
        }
      } else {
        _storedValue = null;
      }
    }

    public Builder(
        final _B _parentBuilder,
        final PartyCreationRequestDTO _other,
        final boolean _copy,
        final PropertyTree _propertyTree,
        final PropertyTreeUse _propertyTreeUse) {
      this._parentBuilder = _parentBuilder;
      if (_other != null) {
        if (_copy) {
          _storedValue = null;
          final PropertyTree sampleUnitRefPropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("sampleUnitRef"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (sampleUnitRefPropertyTree != null)
              : ((sampleUnitRefPropertyTree == null) || (!sampleUnitRefPropertyTree.isLeaf())))) {
            this.sampleUnitRef = _other.sampleUnitRef;
          }
          final PropertyTree sampleSummaryIdPropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("sampleSummaryId"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (sampleSummaryIdPropertyTree != null)
              : ((sampleSummaryIdPropertyTree == null)
                  || (!sampleSummaryIdPropertyTree.isLeaf())))) {
            this.sampleSummaryId = _other.sampleSummaryId;
          }
          final PropertyTree sampleUnitTypePropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("sampleUnitType"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (sampleUnitTypePropertyTree != null)
              : ((sampleUnitTypePropertyTree == null) || (!sampleUnitTypePropertyTree.isLeaf())))) {
            this.sampleUnitType = _other.sampleUnitType;
          }
          final PropertyTree attributesPropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("attributes"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (attributesPropertyTree != null)
              : ((attributesPropertyTree == null) || (!attributesPropertyTree.isLeaf())))) {
            this.attributes =
                ((_other.attributes == null)
                    ? null
                    : _other.attributes.newCopyBuilder(
                        this, attributesPropertyTree, _propertyTreeUse));
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

    protected <_P extends PartyCreationRequestDTO> _P init(final _P _product) {
      _product.sampleUnitRef = this.sampleUnitRef;
      _product.sampleSummaryId = this.sampleSummaryId;
      _product.sampleUnitType = this.sampleUnitType;
      _product.attributes = ((this.attributes == null) ? null : this.attributes.build());
      return _product;
    }

    /**
     * Sets the new value of "sampleUnitRef" (any previous value will be replaced)
     *
     * @param sampleUnitRef New value of the "sampleUnitRef" property.
     */
    public PartyCreationRequestDTO.Builder<_B> withSampleUnitRef(final String sampleUnitRef) {
      this.sampleUnitRef = sampleUnitRef;
      return this;
    }

    /**
     * Sets the new value of "sampleSummaryId" (any previous value will be replaced)
     *
     * @param sampleSummaryId New value of the "sampleSummaryId" property.
     */
    public PartyCreationRequestDTO.Builder<_B> withSampleSummaryId(final String sampleSummaryId) {
      this.sampleSummaryId = sampleSummaryId;
      return this;
    }

    /**
     * Sets the new value of "sampleUnitType" (any previous value will be replaced)
     *
     * @param sampleUnitType New value of the "sampleUnitType" property.
     */
    public PartyCreationRequestDTO.Builder<_B> withSampleUnitType(final String sampleUnitType) {
      this.sampleUnitType = sampleUnitType;
      return this;
    }

    /**
     * Sets the new value of "attributes" (any previous value will be replaced)
     *
     * @param attributes New value of the "attributes" property.
     */
    public PartyCreationRequestDTO.Builder<_B> withAttributes(
        final PartyCreationRequestAttributesDTO attributes) {
      this.attributes =
          ((attributes == null)
              ? null
              : new PartyCreationRequestAttributesDTO.Builder<PartyCreationRequestDTO.Builder<_B>>(
                  this, attributes, false));
      return this;
    }

    /**
     * Returns a new builder to build the value of the "attributes" property (replacing previous
     * value). Use {@link
     * uk.gov.ons.ctp.response.collection.exercise.lib.party.definition.PartyCreationRequestAttributesDTO.Builder#end()}
     * to return to the current builder.
     *
     * @return A new builder to build the value of the "attributes" property. Use {@link
     *     uk.gov.ons.ctp.response.collection.exercise.lib.party.definition.PartyCreationRequestAttributesDTO.Builder#end()}
     *     to return to the current builder.
     */
    public PartyCreationRequestAttributesDTO.Builder<? extends PartyCreationRequestDTO.Builder<_B>>
        withAttributes() {
      return this.attributes =
          new PartyCreationRequestAttributesDTO.Builder<PartyCreationRequestDTO.Builder<_B>>(
              this, null, false);
    }

    @Override
    public PartyCreationRequestDTO build() {
      if (_storedValue == null) {
        return this.init(new PartyCreationRequestDTO());
      } else {
        return ((PartyCreationRequestDTO) _storedValue);
      }
    }
  }

  public static class Select
      extends PartyCreationRequestDTO.Selector<PartyCreationRequestDTO.Select, Void> {

    Select() {
      super(null, null, null);
    }

    public static PartyCreationRequestDTO.Select _root() {
      return new PartyCreationRequestDTO.Select();
    }
  }

  public static class Selector<TRoot extends com.kscs.util.jaxb.Selector<TRoot, ?>, TParent>
      extends com.kscs.util.jaxb.Selector<TRoot, TParent> {

    private com.kscs.util.jaxb.Selector<TRoot, PartyCreationRequestDTO.Selector<TRoot, TParent>>
        sampleUnitRef = null;
    private com.kscs.util.jaxb.Selector<TRoot, PartyCreationRequestDTO.Selector<TRoot, TParent>>
        sampleSummaryId = null;
    private com.kscs.util.jaxb.Selector<TRoot, PartyCreationRequestDTO.Selector<TRoot, TParent>>
        sampleUnitType = null;
    private PartyCreationRequestAttributesDTO.Selector<
            TRoot, PartyCreationRequestDTO.Selector<TRoot, TParent>>
        attributes = null;

    public Selector(final TRoot root, final TParent parent, final String propertyName) {
      super(root, parent, propertyName);
    }

    @Override
    public Map<String, PropertyTree> buildChildren() {
      final Map<String, PropertyTree> products = new HashMap<String, PropertyTree>();
      products.putAll(super.buildChildren());
      if (this.sampleUnitRef != null) {
        products.put("sampleUnitRef", this.sampleUnitRef.init());
      }
      if (this.sampleSummaryId != null) {
        products.put("sampleSummaryId", this.sampleSummaryId.init());
      }
      if (this.sampleUnitType != null) {
        products.put("sampleUnitType", this.sampleUnitType.init());
      }
      if (this.attributes != null) {
        products.put("attributes", this.attributes.init());
      }
      return products;
    }

    public com.kscs.util.jaxb.Selector<TRoot, PartyCreationRequestDTO.Selector<TRoot, TParent>>
        sampleUnitRef() {
      return ((this.sampleUnitRef == null)
          ? this.sampleUnitRef =
              new com.kscs.util.jaxb.Selector<
                  TRoot, PartyCreationRequestDTO.Selector<TRoot, TParent>>(
                  this._root, this, "sampleUnitRef")
          : this.sampleUnitRef);
    }

    public com.kscs.util.jaxb.Selector<TRoot, PartyCreationRequestDTO.Selector<TRoot, TParent>>
        sampleSummaryId() {
      return ((this.sampleSummaryId == null)
          ? this.sampleSummaryId =
              new com.kscs.util.jaxb.Selector<
                  TRoot, PartyCreationRequestDTO.Selector<TRoot, TParent>>(
                  this._root, this, "sampleSummaryId")
          : this.sampleSummaryId);
    }

    public com.kscs.util.jaxb.Selector<TRoot, PartyCreationRequestDTO.Selector<TRoot, TParent>>
        sampleUnitType() {
      return ((this.sampleUnitType == null)
          ? this.sampleUnitType =
              new com.kscs.util.jaxb.Selector<
                  TRoot, PartyCreationRequestDTO.Selector<TRoot, TParent>>(
                  this._root, this, "sampleUnitType")
          : this.sampleUnitType);
    }

    public PartyCreationRequestAttributesDTO.Selector<
            TRoot, PartyCreationRequestDTO.Selector<TRoot, TParent>>
        attributes() {
      return ((this.attributes == null)
          ? this.attributes =
              new PartyCreationRequestAttributesDTO.Selector<
                  TRoot, PartyCreationRequestDTO.Selector<TRoot, TParent>>(
                  this._root, this, "attributes")
          : this.attributes);
    }
  }
}
