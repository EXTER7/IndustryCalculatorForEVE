package com.exter.xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class XmlNode
{
  public class Attribute
  {
    private String name;
    private String value;
    
    public String Name()
    {
      return name;
    }
    
    public String Value()
    {
      return value;
    }
    
    public Attribute(String n,String v)
    {
      name = n;
      value = v;
    }
  }
  
  private String name;
  private String text;
  
  private List<XmlNode> subnodes;
  
  private Attribute[] attributes;
  
  private boolean ValidText(String txt)
  {
    int i;
    String chars = "\r\n \t";
    for(i = 0; i < txt.length(); i++)
    {
      String c = String.valueOf(txt.charAt(i));
      if(!chars.contains(c))
      {
        return true;
      }
    }
    
    return false;
  }

  private void Load(XmlPullParser xml,int level) throws XmlNodeException
  {
    int i;
    if(level == 0)
    {
      try
      {
        if(xml.getEventType() != XmlPullParser.START_DOCUMENT)
        {
          throw new XmlNodeException();
        }
        xml.next();
        if(xml.getEventType() != XmlPullParser.START_TAG)
        {
          throw new XmlNodeException();
        }
      } catch (XmlPullParserException | IOException e)
      {
        throw new XmlNodeException();
      }
    }    
    name = xml.getName();
    
    int attr_count = xml.getAttributeCount();
    if(attr_count > 0)
    {
      attributes = new Attribute[attr_count];
      for(i = 0; i < attr_count; i++)
      {
        attributes[i] = new Attribute(xml.getAttributeName(i),xml.getAttributeValue(i));
      }
    }
    ArrayList<XmlNode> sub = new ArrayList<>();
    try
    {
      xml.next();
      while (true)
      {
        int event = xml.getEventType();
        String txt;
        switch (event)
        {
          case XmlPullParser.START_TAG:
            sub.add(new XmlNode(xml,level+1));
            break;
          case XmlPullParser.TEXT:
            txt = xml.getText();
            if(ValidText(txt))
            {
              sub.add(new XmlNode(txt));
            }
            break;
          case XmlPullParser.END_TAG:
            if(sub.size() > 0)
            {
              subnodes = sub;              
            } else
            {
              subnodes = null;
            }
            return;
          case XmlPullParser.END_DOCUMENT:
            if(level != 0)
            {
              throw new XmlNodeException();
            }
            return;
        }
        xml.next();
      }
    } catch (XmlPullParserException | IOException e)
    {
      throw new XmlNodeException();
    }
  }
  
  private XmlNode(String txt)
  {
    name = null;
    text = txt;
    attributes = null;
  }

  private XmlNode(XmlPullParser xml,int level) throws XmlNodeException
  {
    name = null;
    text = null;
    attributes = null;
    Load(xml,level);
  }

  public XmlNode(XmlPullParser xml) throws XmlNodeException
  {
    name = null;
    text = null;
    attributes = null;
    Load(xml,0);
  }

  public String FindAttribute(String name)
  {
    if(attributes == null || name == null)
    {
      return null;
    }
    for(Attribute at:attributes)
    {
      if(at.Name().equals(name))
      {
        return at.Value();
      }
    }
    return null;
  }
  
  public int SubNodeCount()
  {
    if(subnodes == null)
    {
      return 0;
    }
    return subnodes.size();
  }

  public XmlNode SubNode(int i)
  {
    if(subnodes == null || i < 0 || i >= subnodes.size())
    {
      return null;
    }
    return subnodes.get(i);
  }

  public XmlNode FindSubNode(String name)
  {
    for(XmlNode sn:subnodes)
    {
      if(sn.name != null && sn.name.equals(name))
      {
        return sn;
      }
    }
    return null;
  }

  public String Name()
  {
    return name;
  }
  
  public String Text()
  {
    return text;
  }
}
