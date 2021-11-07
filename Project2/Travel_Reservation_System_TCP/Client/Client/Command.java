package Client;

public enum Command {
	Help("List all available commands", "[CommandName]"),

	AddFlight("Add a new flight number", "<xid>,<FlightNumber>,<NumberOfSeats>,<PricePerSeat>"),
	AddCars("Add a new car location", "<xid>,<Location>,<NumberOfCar>,<Price>"),
	AddRooms("Add a new room location", "<xid>,<Location>,<NumberOfRoom>,<Price>"),
	AddCustomer("Generate a new customer id", "<xid>"),
	AddCustomerID("Create a new customer with the id", "<xid>,<CustomerID>"),

	DeleteFlight("Delete a flight number", "<xid>,<FlightNumber>"),
	DeleteCars("Delete all cars at a location", "<xid>,<Location>"),
	DeleteRooms("Delete all rooms at a location", "<xid>,<Location>"),
	DeleteCustomer("Delete a customer (and return all reservations)", "<xid>,<CustomerID>"),

	QueryFlight("Query the number of available seats on a flight number", "<xid>,<FlightNumber>"),
	QueryCars("Query the number of available cars at a location", "<xid>,<Location>"),
	QueryRooms("Query the number of available rooms at a location", "<xid>,<Location>"),
	QueryCustomer("Query a customer's bill", "<xid>,<CustomerID>"),

	QueryFlightPrice("Query the price per seat on a flight number", "<xid>,<FlightNumber>"),
	QueryCarsPrice("Query the price per car at a location", "<xid>,<Location>"),
	QueryRoomsPrice("Query the price per room at a location", "<xid>,<Location>"),

	QueryReservableItems("Query the reservable flights, cars, rooms", "<xid>,<Flights-true/false>,<Cars-true/false>,<Rooms-true/false>"),
	QueryFlightReservers("Query all the customers that have reserved flights", "<xid>"),
	QueryCarReservers("Query all the customers that have reserved cars", "<xid>"),
	QueryRoomReservers("Query all the customers that have reserved rooms", "<xid>"),

	ReserveFlight("Reserve a flight number for a customer", "<xid>,<CustomerID>,<FlightNumber>"),
	ReserveCar("Reserve a car for a customer at a location", "<xid>,<CustomerID>,<Location>"),
	ReserveRoom("Reserve a room for a customer at a location", "<xid>,<CustomerID>,<Location>"),

	Bundle("Book N flight numbers, and optionally a room and/or car at a location", "<xid>,<CustomerID>,<FlightNumber1>...<FlightNumberN>,<Location>,<Car-Y/N>,<Room-Y/N>"),

	Start("Start a transaction", ""),
	Commit("Commit a transaction", "<transaction_id>"),
	Abort("Abort a transaction", "<transaction_id>"),
	Shutdown("Shutdown the system", ""),

	TransactionAddAndQueryFlight("Execute a transaction to add and query a flight", "<FlightNumber>,<NumberOfSeats>,<PricePerSeat>"),
	TransactionAddAndQueryCars("Execute a transaction to add and query cars", "<Location>,<NumberOfCar>,<Price>"),
	TransactionAddAndQueryRooms("Execute a transaction to add and query rooms", "<Location>,<NumberOfRoom>,<Price>"),
	TransactionReserveAll("Execute a transaction to add a customer and reserve all", "<FlightNumber>,<Location>"),

	TestTransactions("Test transactions", "<Transaction Type(number)><NumTransactions>"),


	ExecuteTestSuite("Client can execute test suites with different length transactions", "<SimType><NumTransactions><Debug><Transactions/(s)><TransLength><TransType><ClientName>"),

	Quit("Exit the client application", "");

	String m_description;
	String m_args;

	Command(String p_description, String p_args)
	{
		m_description = p_description;
		m_args = p_args;
	}

	public static Command fromString(String string)
	{
		for (Command cmd : Command.values())
		{
			if (cmd.name().equalsIgnoreCase(string))
			{
				return cmd;
			}
		}
		throw new IllegalArgumentException("Command " + string + " not found");
	}

	public static String description()
	{
		String ret = "Commands supported by the client:\n";
		for (Command cmd : Command.values())
		{	 
			ret += "\t" + cmd.name() + "\n";
		}
		ret += "use help,<CommandName> for more detailed information";
		return ret;
	}

	public String toString()
	{
		String ret = name() + ": " + m_description + "\n";
		ret += "Usage: " + name() + "," + m_args;
		return ret;
	}
}             
