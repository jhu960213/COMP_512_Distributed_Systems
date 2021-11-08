// -------------------------------
// Kevin T. Manley
// CSE 593
// -------------------------------

package Server.Common;

import java.io.*;

// Superclass for the three reservable items: Flight, Car, and Room
public abstract class ReservableItem extends RMItem implements Serializable
{
	private int m_nCount;
	private int m_nPrice;
	private int m_nReserved;
	private String m_location;

	public ReservableItem(String location, int count, int price)
	{
		super();
		this.m_location = location;
		this.m_nCount = count;
		this.m_nPrice = price;
		this.m_nReserved = 0;
	}

	public void setCount(int count)
	{
		this.m_nCount = count;
	}

	public int getCount()
	{
		return this.m_nCount;
	}

	public void setPrice(int price)
	{
		this.m_nPrice = price;
	}

	public int getPrice()
	{
		return this.m_nPrice;
	}

	public void setReserved(int r)
	{
		this.m_nReserved = r;
	}

	public int getReserved()
	{
		return this.m_nReserved;
	}

	public String getLocation()
	{
		return this.m_location;
	}

	public String toString()
	{
		return "RESERVABLEITEM key='" + getKey() + "', location='" + getLocation() +
			"', count='" + getCount() + "', price='" + getPrice() + "'";
	}

	public abstract String getKey();

	public Object clone()
	{
		ReservableItem obj = (ReservableItem)super.clone();
		obj.m_location = this.m_location;
		obj.m_nCount = this.m_nCount;
		obj.m_nPrice = this.m_nPrice;
		obj.m_nReserved = this.m_nReserved;
		return obj;
	}
}

