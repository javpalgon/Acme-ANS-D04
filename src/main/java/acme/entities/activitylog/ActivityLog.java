
package acme.entities.activitylog;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import acme.client.components.basis.AbstractEntity;
import acme.client.components.mappings.Automapped;
import acme.client.components.validation.Mandatory;
import acme.client.components.validation.ValidMoment;
import acme.client.components.validation.ValidNumber;
import acme.client.components.validation.ValidString;
import acme.entities.assignment.Assignment;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "activity_log", indexes = {
	@Index(name = "idx_activity_log_assignment", columnList = "assignment_id"), @Index(name = "idx_activity_log_draft_mode", columnList = "draftMode"), @Index(name = "idx_activity_log_registered_at", columnList = "registeredAt")
})
public class ActivityLog extends AbstractEntity {

	// Serialisation version -------------------------------------------

	private static final long	serialVersionUID	= 1L;

	// Attributes --------------------------------------------------------

	@Mandatory
	@ValidMoment(past = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date				registeredAt;

	@Mandatory
	@NotBlank
	@ValidString(max = 50)
	@Automapped
	private String				incidentType;

	@Mandatory
	@NotBlank
	@ValidString(max = 255)
	@Automapped
	private String				description;

	@Mandatory
	@ValidNumber(min = 0, max = 10)
	@Automapped
	private Integer				severityLevel;

	@Mandatory
	@Valid
	@ManyToOne(optional = false)
	private Assignment			assignment;

	@Mandatory
	@Automapped
	private Boolean				draftMode;

}
