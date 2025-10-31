package uk.gov.ons.ctp.response.collection.exercise.lib.party.definition;

import com.kscs.util.jaxb.Buildable;
import com.kscs.util.jaxb.PropertyTree;
import com.kscs.util.jaxb.PropertyTreeUse;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.HashMap;
import java.util.Map;

/**
 * Java class for Enrolment complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="Enrolment"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="enrolmentStatus" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="surveyId" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "Enrolment",
    propOrder = {"enrolmentStatus", "name", "surveyId"})
public class Enrolment {

  @XmlElement(required = true)
  protected String enrolmentStatus;

  @XmlElement(required = true)
  protected String name;

  @XmlElement(required = true)
  protected String surveyId;

  /** Default no-arg constructor */
  public Enrolment() {
    super();
  }

  /** Fully-initialising value constructor */
  public Enrolment(final String enrolmentStatus, final String name, final String surveyId) {
    this.enrolmentStatus = enrolmentStatus;
    this.name = name;
    this.surveyId = surveyId;
  }

  /**
   * Gets the value of the enrolmentStatus property.
   *
   * @return possible object is {@link String }
   */
  public String getEnrolmentStatus() {
    return enrolmentStatus;
  }

  /**
   * Sets the value of the enrolmentStatus property.
   *
   * @param value allowed object is {@link String }
   */
  public void setEnrolmentStatus(String value) {
    this.enrolmentStatus = value;
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
   * Gets the value of the surveyId property.
   *
   * @return possible object is {@link String }
   */
  public String getSurveyId() {
    return surveyId;
  }

  /**
   * Sets the value of the surveyId property.
   *
   * @param value allowed object is {@link String }
   */
  public void setSurveyId(String value) {
    this.surveyId = value;
  }

  /**
   * Copies all state of this object to a builder. This method is used by the {@link #copyOf} method
   * and should not be called directly by client code.
   *
   * @param _other A builder instance to which the state of this object will be copied.
   */
  public <_B> void copyTo(final Enrolment.Builder<_B> _other) {
    _other.enrolmentStatus = this.enrolmentStatus;
    _other.name = this.name;
    _other.surveyId = this.surveyId;
  }

  public <_B> Enrolment.Builder<_B> newCopyBuilder(final _B _parentBuilder) {
    return new Enrolment.Builder<_B>(_parentBuilder, this, true);
  }

  public Enrolment.Builder<Void> newCopyBuilder() {
    return newCopyBuilder(null);
  }

  public static Enrolment.Builder<Void> builder() {
    return new Enrolment.Builder<Void>(null, null, false);
  }

  public static <_B> Enrolment.Builder<_B> copyOf(final Enrolment _other) {
    final Enrolment.Builder<_B> _newBuilder = new Enrolment.Builder<_B>(null, null, false);
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
      final Enrolment.Builder<_B> _other,
      final PropertyTree _propertyTree,
      final PropertyTreeUse _propertyTreeUse) {
    final PropertyTree enrolmentStatusPropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("enrolmentStatus"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (enrolmentStatusPropertyTree != null)
        : ((enrolmentStatusPropertyTree == null) || (!enrolmentStatusPropertyTree.isLeaf())))) {
      _other.enrolmentStatus = this.enrolmentStatus;
    }
    final PropertyTree namePropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("name"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (namePropertyTree != null)
        : ((namePropertyTree == null) || (!namePropertyTree.isLeaf())))) {
      _other.name = this.name;
    }
    final PropertyTree surveyIdPropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("surveyId"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (surveyIdPropertyTree != null)
        : ((surveyIdPropertyTree == null) || (!surveyIdPropertyTree.isLeaf())))) {
      _other.surveyId = this.surveyId;
    }
  }

  public <_B> Enrolment.Builder<_B> newCopyBuilder(
      final _B _parentBuilder,
      final PropertyTree _propertyTree,
      final PropertyTreeUse _propertyTreeUse) {
    return new Enrolment.Builder<_B>(_parentBuilder, this, true, _propertyTree, _propertyTreeUse);
  }

  public Enrolment.Builder<Void> newCopyBuilder(
      final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
    return newCopyBuilder(null, _propertyTree, _propertyTreeUse);
  }

  public static <_B> Enrolment.Builder<_B> copyOf(
      final Enrolment _other,
      final PropertyTree _propertyTree,
      final PropertyTreeUse _propertyTreeUse) {
    final Enrolment.Builder<_B> _newBuilder = new Enrolment.Builder<_B>(null, null, false);
    _other.copyTo(_newBuilder, _propertyTree, _propertyTreeUse);
    return _newBuilder;
  }

  public static Enrolment.Builder<Void> copyExcept(
      final Enrolment _other, final PropertyTree _propertyTree) {
    return copyOf(_other, _propertyTree, PropertyTreeUse.EXCLUDE);
  }

  public static Enrolment.Builder<Void> copyOnly(
      final Enrolment _other, final PropertyTree _propertyTree) {
    return copyOf(_other, _propertyTree, PropertyTreeUse.INCLUDE);
  }

  public static class Builder<_B> implements Buildable {

    protected final _B _parentBuilder;
    protected final Enrolment _storedValue;
    private String enrolmentStatus;
    private String name;
    private String surveyId;

    public Builder(final _B _parentBuilder, final Enrolment _other, final boolean _copy) {
      this._parentBuilder = _parentBuilder;
      if (_other != null) {
        if (_copy) {
          _storedValue = null;
          this.enrolmentStatus = _other.enrolmentStatus;
          this.name = _other.name;
          this.surveyId = _other.surveyId;
        } else {
          _storedValue = _other;
        }
      } else {
        _storedValue = null;
      }
    }

    public Builder(
        final _B _parentBuilder,
        final Enrolment _other,
        final boolean _copy,
        final PropertyTree _propertyTree,
        final PropertyTreeUse _propertyTreeUse) {
      this._parentBuilder = _parentBuilder;
      if (_other != null) {
        if (_copy) {
          _storedValue = null;
          final PropertyTree enrolmentStatusPropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("enrolmentStatus"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (enrolmentStatusPropertyTree != null)
              : ((enrolmentStatusPropertyTree == null)
                  || (!enrolmentStatusPropertyTree.isLeaf())))) {
            this.enrolmentStatus = _other.enrolmentStatus;
          }
          final PropertyTree namePropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("name"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (namePropertyTree != null)
              : ((namePropertyTree == null) || (!namePropertyTree.isLeaf())))) {
            this.name = _other.name;
          }
          final PropertyTree surveyIdPropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("surveyId"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (surveyIdPropertyTree != null)
              : ((surveyIdPropertyTree == null) || (!surveyIdPropertyTree.isLeaf())))) {
            this.surveyId = _other.surveyId;
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

    protected <_P extends Enrolment> _P init(final _P _product) {
      _product.enrolmentStatus = this.enrolmentStatus;
      _product.name = this.name;
      _product.surveyId = this.surveyId;
      return _product;
    }

    /**
     * Sets the new value of "enrolmentStatus" (any previous value will be replaced)
     *
     * @param enrolmentStatus New value of the "enrolmentStatus" property.
     */
    public Enrolment.Builder<_B> withEnrolmentStatus(final String enrolmentStatus) {
      this.enrolmentStatus = enrolmentStatus;
      return this;
    }

    /**
     * Sets the new value of "name" (any previous value will be replaced)
     *
     * @param name New value of the "name" property.
     */
    public Enrolment.Builder<_B> withName(final String name) {
      this.name = name;
      return this;
    }

    /**
     * Sets the new value of "surveyId" (any previous value will be replaced)
     *
     * @param surveyId New value of the "surveyId" property.
     */
    public Enrolment.Builder<_B> withSurveyId(final String surveyId) {
      this.surveyId = surveyId;
      return this;
    }

    @Override
    public Enrolment build() {
      if (_storedValue == null) {
        return this.init(new Enrolment());
      } else {
        return ((Enrolment) _storedValue);
      }
    }
  }

  public static class Select extends Enrolment.Selector<Enrolment.Select, Void> {

    Select() {
      super(null, null, null);
    }

    public static Enrolment.Select _root() {
      return new Enrolment.Select();
    }
  }

  public static class Selector<TRoot extends com.kscs.util.jaxb.Selector<TRoot, ?>, TParent>
      extends com.kscs.util.jaxb.Selector<TRoot, TParent> {

    private com.kscs.util.jaxb.Selector<TRoot, Enrolment.Selector<TRoot, TParent>> enrolmentStatus =
        null;
    private com.kscs.util.jaxb.Selector<TRoot, Enrolment.Selector<TRoot, TParent>> name = null;
    private com.kscs.util.jaxb.Selector<TRoot, Enrolment.Selector<TRoot, TParent>> surveyId = null;

    public Selector(final TRoot root, final TParent parent, final String propertyName) {
      super(root, parent, propertyName);
    }

    @Override
    public Map<String, PropertyTree> buildChildren() {
      final Map<String, PropertyTree> products = new HashMap<String, PropertyTree>();
      products.putAll(super.buildChildren());
      if (this.enrolmentStatus != null) {
        products.put("enrolmentStatus", this.enrolmentStatus.init());
      }
      if (this.name != null) {
        products.put("name", this.name.init());
      }
      if (this.surveyId != null) {
        products.put("surveyId", this.surveyId.init());
      }
      return products;
    }

    public com.kscs.util.jaxb.Selector<TRoot, Enrolment.Selector<TRoot, TParent>>
        enrolmentStatus() {
      return ((this.enrolmentStatus == null)
          ? this.enrolmentStatus =
              new com.kscs.util.jaxb.Selector<TRoot, Enrolment.Selector<TRoot, TParent>>(
                  this._root, this, "enrolmentStatus")
          : this.enrolmentStatus);
    }

    public com.kscs.util.jaxb.Selector<TRoot, Enrolment.Selector<TRoot, TParent>> name() {
      return ((this.name == null)
          ? this.name =
              new com.kscs.util.jaxb.Selector<TRoot, Enrolment.Selector<TRoot, TParent>>(
                  this._root, this, "name")
          : this.name);
    }

    public com.kscs.util.jaxb.Selector<TRoot, Enrolment.Selector<TRoot, TParent>> surveyId() {
      return ((this.surveyId == null)
          ? this.surveyId =
              new com.kscs.util.jaxb.Selector<TRoot, Enrolment.Selector<TRoot, TParent>>(
                  this._root, this, "surveyId")
          : this.surveyId);
    }
  }
}
