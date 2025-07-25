
package acme.features.any.weather;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.principals.Any;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.weather.Weather;

@GuiService
public class AnyWeatherShowService extends AbstractGuiService<Any, Weather> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private AnyWeatherRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		super.getResponse().setAuthorised(true);
	}

	@Override
	public void load() {
		Weather weather;
		int weatherId;

		weatherId = super.getRequest().getData("id", int.class);
		weather = this.repository.findWeatherById(weatherId);

		super.getBuffer().addData(weather);
	}

	@Override
	public void unbind(final Weather weather) {
		Dataset dataset;
		dataset = super.unbindObject(weather, "city", "timestamp", "weatherMain", "weatherDescription", "visibility", "windSpeed", "isBadWeather");
		super.getResponse().addData(dataset);
	}

}
