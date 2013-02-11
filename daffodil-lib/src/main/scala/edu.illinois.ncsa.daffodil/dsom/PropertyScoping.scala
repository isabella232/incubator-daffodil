package daffodil.dsom

import daffodil.exceptions.SchemaFileLocatable
import daffodil.util.Info
import daffodil.util.Debug
import daffodil.util.Logging
import daffodil.schema.annotation.props.PropertyMixin
import daffodil.xml.GetAttributesMixin

/**
 * This file is classes and traits to implement
 * property lookups, diagnostics about them, and the
 * lexical scoping of properties from the schema-document level.
 *
 * The ultimate data structure is two sequences, one for nonDefault,
 * one for default. Each such sequence is a sequence of ChainRropProviders.
 * Each of those is a sequence of LeafPropProviders
 */

/**
 * Result of searching for a property.
 *
 * Includes location information so you can issue a diagnostic
 * about where a conflicting property
 * definition was found, or all the places where one searched
 * but did not find a property definition.
 *
 * One must also have the location object because it must
 * be used if the property value contains a QName (or is an expression
 * containing QNames). That is, you can't interpret a QName without
 * the scope from the XML where it was written.
 */
sealed abstract class PropertyLookupResult
case class Found(value: String, location: LookupLocation) extends PropertyLookupResult
case class NotFound(localWhereLooked: Seq[LookupLocation], defaultWhereLooked: Seq[LookupLocation]) extends PropertyLookupResult

/**
 * A lookup location is where we indicate a property binding
 * resides. This can be an annotation object (ex: a dfdl:sequence or a dfdl:format)
 * or in the case of short-form properties it could be the
 * annotated schema component itself.
 *
 * The point is to get the file and line number information right
 * so we point the user at the right place.
 *
 * It also resolves QNames in the right way. So given a property value that
 * contains a QName, the associated LookupLocation can be used to resolve that
 * QName to a namespace and a local name.
 */
trait LookupLocation
  extends SchemaFileLocatable
  with ResolvesQNames
  with GetAttributesMixin

trait PropTypes {
  /**
   * type of a map entry which maps a property name as key, to a property value,
   * and along side it is a LookupLocation object telling us where we found that
   * property value.
   *
   * The property maps/lists in DFDLFormatAnnotation have been enhanced to have
   * this LookupLocation thing along their side to allow issuing
   * better diagnostic messages about conflicting property definitions
   */
  type PropItem = (String, (String, LookupLocation))

  type PropMap = Map[String, (String, LookupLocation)]

  final val emptyPropMap = Map.empty.asInstanceOf[PropMap]
}

trait FindPropertyMixin extends PropTypes {

  /**
   * Implemented by users of the mixin so that we can
   * report a property not found error.
   */
  def SDE(str: String, args: Any*): Nothing

  /**
   * Implemented in various ways by users of the mixin.
   */
  def findPropertyOption(pname: String): PropertyLookupResult

  /**
   * Call this to find/get a property.
   *
   * Property values are non-optional in DFDL. If they're not
   * there but a format requires them, then it's always an error.
   *
   * Note also that DFDL doesn't have default values for properties. That means
   * that most use of properties is unconditional. Get the property value, and
   * it must be there, or its an error. There are very few exceptions to this
   * rule.
   */
  def findProperty(pname: String): PropertyLookupResult = {
    val res = findPropertyOption(pname)
    res match {
      case f: Found => f
      case NotFound(nonDefaultLocs, defaultLocs) => SDE("Property %s is not defined.\nSearched these locations: %s\n Searched these default locations: %s.",
        pname, nonDefaultLocs, defaultLocs)
    }
    res
  }

  /**
   * It is ok to use getProperty if the resulting property value cannot ever contain
   * a QName that would have to be resolved.
   */
  def getProperty(pname: String): String = {
    val Found(res, _) = findProperty(pname)
    res
  }

  /**
   * Don't use this much. If an SDE needs to be reported, you won't have
   * the source of the property to put into the message. Use findPropertyOption
   * instead. That returns the value and the LookupLocation where it was
   * found for use in diagnostics.
   *
   * Also, don't use if the property value could ever contain a QName, because
   * one needs that LookupLocation to resolve QNames properly.
   *
   * Note: any expression can contain QNames, so no expression-valued property or
   * one that could be a value or an expression, should ever use this or getProperty.
   *
   * See JIRA DFDL-506.
   */
  def getPropertyOption(pname: String): Option[String] = {
    val lookupRes = findPropertyOption(pname)
    val res = lookupRes match {
      case Found(v, _) => Some(v)
      case _ => None
    }
    res
  }

  /**
   * For unit testing convenience
   */
  def verifyPropValue(key: String, value: String): Boolean = {
    findPropertyOption(key) match {
      case Found(`value`, _) => true
      case NotFound(_, _) => false
    }
  }
}