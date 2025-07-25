
package acme.features.manager.leg;

import java.util.Collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import acme.client.repositories.AbstractRepository;
import acme.client.services.GuiService;
import acme.entities.aircraft.Aircraft;
import acme.entities.aircraft.AircraftStatus;
import acme.entities.airport.Airport;
import acme.entities.flight.Flight;
import acme.entities.leg.Leg;

@GuiService
public interface ManagerLegRepository extends AbstractRepository {

	@Query("select f from Flight f where f.id = :id")
	Flight findFlightById(int id);

	@Query("select l from Leg l where l.flight.id = :flightId")
	Collection<Leg> findLegsByFlightId(int flightId);

	@Query("select a from Aircraft a where a.id = :id")
	Aircraft findAircraftById(int id);

	@Query("select a from Aircraft a where a.aircraftStatus = :status ")
	Collection<Aircraft> findAllActiveAircrafts(@Param("status") AircraftStatus status);

	@Query("select l from Leg l where l.id = :legId")
	Leg getLegById(int legId);

	@Query("select l.flight from Leg l where l.id = :legId")
	Flight getFlightByLegId(int legId);

	@Query("select a from Airport a")
	Collection<Airport> findAllAirports();

	@Query("select a from Airport a where a.id = :id")
	Airport findAirportById(int id);

	@Query("select l from Leg l where l.isDraftMode = false")
	Collection<Leg> findAllPublishedLegs();

}
