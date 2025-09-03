package uk.gov.ons.ctp.response.collection.exercise.lib.party.definition;

import com.kscs.util.jaxb.Buildable;
import com.kscs.util.jaxb.PropertyTree;
import com.kscs.util.jaxb.PropertyTreeUse;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.*;

/**
 * Java class for Association complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="Association"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="partyId" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="enrolments" type="{http://ons.gov.uk/ctp/response/party/definition}Enrolment" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "Association",
    propOrder = {"partyId", "enrolments"})
public class Association {

  @XmlElement(required = true)
  protected String partyId;

  protected List<Enrolment> enrolments;

  /** Default no-arg constructor */
  public Association() {
    super();
  }

  /** Fully-initialising value constructor */
  public Association(final String partyId, final List<Enrolment> enrolments) {
    this.partyId = partyId;
    this.enrolments = enrolments;
  }

  /**
   * Gets the value of the partyId property.
   *
   * @return possible object is {@link String }
   */
  public String getPartyId() {
    return partyId;
  }

  /**
   * Sets the value of the partyId property.
   *
   * @param value allowed object is {@link String }
   */
  public void setPartyId(String value) {
    this.partyId = value;
  }

  /**
   * Gets the value of the enrolments property.
   *
   * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore any
   * modification you make to the returned list will be present inside the JAXB object. This is why
   * there is not a <CODE>set</CODE> method for the enrolments property.
   *
   * <p>For example, to add a new item, do as follows:
   *
   * <pre>
   *    getEnrolments().add(newItem);
   * </pre>
   *
   * <p>Objects of the following type(s) are allowed in the list {@link Enrolment }
   */
  public List<Enrolment> getEnrolments() {
    if (enrolments == null) {
      enrolments = new ArrayList<Enrolment>();
    }
    return this.enrolments;
  }

  /**
   * Copies all state of this object to a builder. This method is used by the {@link #copyOf} method
   * and should not be called directly by client code.
   *
   * @param _other A builder instance to which the state of this object will be copied.
   */
  public <_B> void copyTo(final Association.Builder<_B> _other) {
    _other.partyId = this.partyId;
    if (this.enrolments == null) {
      _other.enrolments = null;
    } else {
      _other.enrolments = new ArrayList<Enrolment.Builder<Builder<_B>>>();
      for (Enrolment _item : this.enrolments) {
        _other.enrolments.add(((_item == null) ? null : _item.newCopyBuilder(_other)));
      }
    }
  }

  public <_B> Association.Builder<_B> newCopyBuilder(final _B _parentBuilder) {
    return new Association.Builder<_B>(_parentBuilder, this, true);
  }

  public Association.Builder<Void> newCopyBuilder() {
    return newCopyBuilder(null);
  }

  public static Association.Builder<Void> builder() {
    return new Association.Builder<Void>(null, null, false);
  }

  public static <_B> Association.Builder<_B> copyOf(final Association _other) {
    final Association.Builder<_B> _newBuilder = new Association.Builder<_B>(null, null, false);
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
      final Association.Builder<_B> _other,
      final PropertyTree _propertyTree,
      final PropertyTreeUse _propertyTreeUse) {
    final PropertyTree partyIdPropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("partyId"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (partyIdPropertyTree != null)
        : ((partyIdPropertyTree == null) || (!partyIdPropertyTree.isLeaf())))) {
      _other.partyId = this.partyId;
    }
    final PropertyTree enrolmentsPropertyTree =
        ((_propertyTree == null) ? null : _propertyTree.get("enrolments"));
    if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
        ? (enrolmentsPropertyTree != null)
        : ((enrolmentsPropertyTree == null) || (!enrolmentsPropertyTree.isLeaf())))) {
      if (this.enrolments == null) {
        _other.enrolments = null;
      } else {
        _other.enrolments = new ArrayList<Enrolment.Builder<Builder<_B>>>();
        for (Enrolment _item : this.enrolments) {
          _other.enrolments.add(
              ((_item == null)
                  ? null
                  : _item.newCopyBuilder(_other, enrolmentsPropertyTree, _propertyTreeUse)));
        }
      }
    }
  }

  public <_B> Association.Builder<_B> newCopyBuilder(
      final _B _parentBuilder,
      final PropertyTree _propertyTree,
      final PropertyTreeUse _propertyTreeUse) {
    return new Association.Builder<_B>(_parentBuilder, this, true, _propertyTree, _propertyTreeUse);
  }

  public Association.Builder<Void> newCopyBuilder(
      final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
    return newCopyBuilder(null, _propertyTree, _propertyTreeUse);
  }

  public static <_B> Association.Builder<_B> copyOf(
      final Association _other,
      final PropertyTree _propertyTree,
      final PropertyTreeUse _propertyTreeUse) {
    final Association.Builder<_B> _newBuilder = new Association.Builder<_B>(null, null, false);
    _other.copyTo(_newBuilder, _propertyTree, _propertyTreeUse);
    return _newBuilder;
  }

  public static Association.Builder<Void> copyExcept(
      final Association _other, final PropertyTree _propertyTree) {
    return copyOf(_other, _propertyTree, PropertyTreeUse.EXCLUDE);
  }

  public static Association.Builder<Void> copyOnly(
      final Association _other, final PropertyTree _propertyTree) {
    return copyOf(_other, _propertyTree, PropertyTreeUse.INCLUDE);
  }

  public static class Builder<_B> implements Buildable {

    protected final _B _parentBuilder;
    protected final Association _storedValue;
    private String partyId;
    private List<Enrolment.Builder<Builder<_B>>> enrolments;

    public Builder(final _B _parentBuilder, final Association _other, final boolean _copy) {
      this._parentBuilder = _parentBuilder;
      if (_other != null) {
        if (_copy) {
          _storedValue = null;
          this.partyId = _other.partyId;
          if (_other.enrolments == null) {
            this.enrolments = null;
          } else {
            this.enrolments = new ArrayList<Enrolment.Builder<Builder<_B>>>();
            for (Enrolment _item : _other.enrolments) {
              this.enrolments.add(((_item == null) ? null : _item.newCopyBuilder(this)));
            }
          }
        } else {
          _storedValue = _other;
        }
      } else {
        _storedValue = null;
      }
    }

    public Builder(
        final _B _parentBuilder,
        final Association _other,
        final boolean _copy,
        final PropertyTree _propertyTree,
        final PropertyTreeUse _propertyTreeUse) {
      this._parentBuilder = _parentBuilder;
      if (_other != null) {
        if (_copy) {
          _storedValue = null;
          final PropertyTree partyIdPropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("partyId"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (partyIdPropertyTree != null)
              : ((partyIdPropertyTree == null) || (!partyIdPropertyTree.isLeaf())))) {
            this.partyId = _other.partyId;
          }
          final PropertyTree enrolmentsPropertyTree =
              ((_propertyTree == null) ? null : _propertyTree.get("enrolments"));
          if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)
              ? (enrolmentsPropertyTree != null)
              : ((enrolmentsPropertyTree == null) || (!enrolmentsPropertyTree.isLeaf())))) {
            if (_other.enrolments == null) {
              this.enrolments = null;
            } else {
              this.enrolments = new ArrayList<Enrolment.Builder<Builder<_B>>>();
              for (Enrolment _item : _other.enrolments) {
                this.enrolments.add(
                    ((_item == null)
                        ? null
                        : _item.newCopyBuilder(this, enrolmentsPropertyTree, _propertyTreeUse)));
              }
            }
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

    protected <_P extends Association> _P init(final _P _product) {
      _product.partyId = this.partyId;
      if (this.enrolments != null) {
        final List<Enrolment> enrolments = new ArrayList<Enrolment>(this.enrolments.size());
        for (Enrolment.Builder<Association.Builder<_B>> _item : this.enrolments) {
          enrolments.add(_item.build());
        }
        _product.enrolments = enrolments;
      }
      return _product;
    }

    /**
     * Sets the new value of "partyId" (any previous value will be replaced)
     *
     * @param partyId New value of the "partyId" property.
     */
    public Association.Builder<_B> withPartyId(final String partyId) {
      this.partyId = partyId;
      return this;
    }

    /**
     * Adds the given items to the value of "enrolments"
     *
     * @param enrolments Items to add to the value of the "enrolments" property
     */
    public Association.Builder<_B> addEnrolments(final Iterable<? extends Enrolment> enrolments) {
      if (enrolments != null) {
        if (this.enrolments == null) {
          this.enrolments = new ArrayList<Enrolment.Builder<Builder<_B>>>();
        }
        for (Enrolment _item : enrolments) {
          this.enrolments.add(new Enrolment.Builder<Association.Builder<_B>>(this, _item, false));
        }
      }
      return this;
    }

    /**
     * Sets the new value of "enrolments" (any previous value will be replaced)
     *
     * @param enrolments New value of the "enrolments" property.
     */
    public Association.Builder<_B> withEnrolments(final Iterable<? extends Enrolment> enrolments) {
      if (this.enrolments != null) {
        this.enrolments.clear();
      }
      return addEnrolments(enrolments);
    }

    /**
     * Adds the given items to the value of "enrolments"
     *
     * @param enrolments Items to add to the value of the "enrolments" property
     */
    public Association.Builder<_B> addEnrolments(Enrolment... enrolments) {
      addEnrolments(Arrays.asList(enrolments));
      return this;
    }

    /**
     * Sets the new value of "enrolments" (any previous value will be replaced)
     *
     * @param enrolments New value of the "enrolments" property.
     */
    public Association.Builder<_B> withEnrolments(Enrolment... enrolments) {
      withEnrolments(Arrays.asList(enrolments));
      return this;
    }

    /**
     * Returns a new builder to build an additional value of the "Enrolments" property. Use {@link
     * uk.gov.ons.ctp.response.collection.exercise.lib.party.definition.Enrolment.Builder#end()} to
     * return to the current builder.
     *
     * @return a new builder to build an additional value of the "Enrolments" property. Use {@link
     *     uk.gov.ons.ctp.response.collection.exercise.lib.party.definition.Enrolment.Builder#end()}
     *     to return to the current builder.
     */
    public Enrolment.Builder<? extends Association.Builder<_B>> addEnrolments() {
      if (this.enrolments == null) {
        this.enrolments = new ArrayList<Enrolment.Builder<Builder<_B>>>();
      }
      final Enrolment.Builder<Association.Builder<_B>> enrolments_Builder =
          new Enrolment.Builder<Association.Builder<_B>>(this, null, false);
      this.enrolments.add(enrolments_Builder);
      return enrolments_Builder;
    }

    @Override
    public Association build() {
      if (_storedValue == null) {
        return this.init(new Association());
      } else {
        return ((Association) _storedValue);
      }
    }
  }

  public static class Select extends Association.Selector<Association.Select, Void> {

    Select() {
      super(null, null, null);
    }

    public static Association.Select _root() {
      return new Association.Select();
    }
  }

  public static class Selector<TRoot extends com.kscs.util.jaxb.Selector<TRoot, ?>, TParent>
      extends com.kscs.util.jaxb.Selector<TRoot, TParent> {

    private com.kscs.util.jaxb.Selector<TRoot, Association.Selector<TRoot, TParent>> partyId = null;
    private Enrolment.Selector<TRoot, Association.Selector<TRoot, TParent>> enrolments = null;

    public Selector(final TRoot root, final TParent parent, final String propertyName) {
      super(root, parent, propertyName);
    }

    @Override
    public Map<String, PropertyTree> buildChildren() {
      final Map<String, PropertyTree> products = new HashMap<String, PropertyTree>();
      products.putAll(super.buildChildren());
      if (this.partyId != null) {
        products.put("partyId", this.partyId.init());
      }
      if (this.enrolments != null) {
        products.put("enrolments", this.enrolments.init());
      }
      return products;
    }

    public com.kscs.util.jaxb.Selector<TRoot, Association.Selector<TRoot, TParent>> partyId() {
      return ((this.partyId == null)
          ? this.partyId =
              new com.kscs.util.jaxb.Selector<TRoot, Association.Selector<TRoot, TParent>>(
                  this._root, this, "partyId")
          : this.partyId);
    }

    public Enrolment.Selector<TRoot, Association.Selector<TRoot, TParent>> enrolments() {
      return ((this.enrolments == null)
          ? this.enrolments =
              new Enrolment.Selector<TRoot, Association.Selector<TRoot, TParent>>(
                  this._root, this, "enrolments")
          : this.enrolments);
    }
  }
}
