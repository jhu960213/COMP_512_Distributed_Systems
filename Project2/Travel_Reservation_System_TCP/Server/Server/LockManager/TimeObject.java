package Server.LockManager;

import java.util.Date;

public class TimeObject extends TransactionObject
{
	private Date m_date;

	// The data members inherited are
	// TransactionObject:: private int m_xid;

	TimeObject()
	{
		super();
		this.m_date = new Date();
	}

	TimeObject(int xid)
	{
		super(xid);
		this.m_date = new Date();
	}

	public long getTime()
	{
		return this.m_date.getTime();
	}
}
