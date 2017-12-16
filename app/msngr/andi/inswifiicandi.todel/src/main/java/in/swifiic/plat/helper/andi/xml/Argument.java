package in.swifiic.plat.helper.andi.xml;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.Attribute;

/*
143 ^I^I^I<xs:element name="argument" minOccurs="0" maxOccurs="32">$
144 ^I^I^I^I<xs:complexType>$
145 ^I^I^I^I^I<xs:attribute type="xs:string" name="argName" />$
146 ^I^I^I^I^I<xs:attribute type="xs:string" name="argValue" />$
147 ^I^I^I^I</xs:complexType>$
148 ^I^I^I</xs:element>$
*/
@Root(name="argument")
public class Argument {
	@Attribute
	protected String argName;
	
	@Attribute
	protected String argValue;
	
	
	public Argument() {
		
	}
}

