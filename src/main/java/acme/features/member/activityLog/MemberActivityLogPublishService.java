
package acme.features.member.activityLog;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.activitylog.ActivityLog;
import acme.entities.assignment.Assignment;
import acme.entities.assignment.AssignmentStatus;
import acme.entities.flightcrewmember.AvailabilityStatus;
import acme.realms.Member;

@GuiService
public class MemberActivityLogPublishService extends AbstractGuiService<Member, ActivityLog> {

	@Autowired
	private MemberActivityLogRepository repository;


	@Override
	public void authorise() {
		boolean status;
		int activityLogId;
		Assignment assignment;
		ActivityLog activityLog;
		int memberId;

		memberId = super.getRequest().getPrincipal().getActiveRealm().getId();
		activityLogId = super.getRequest().getData("id", int.class);
		assignment = this.repository.findAssignmentByActivityLogId(activityLogId);
		activityLog = this.repository.findActivityLogById(activityLogId);

		status = activityLog.getDraftMode() && assignment != null && assignment.getMember().getAvailabilityStatus().equals(AvailabilityStatus.AVAILABLE) && !assignment.getDraftMode() && assignment.getMember().getId() == memberId
			&& !assignment.getStatus().equals(AssignmentStatus.CANCELLED);

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		ActivityLog activityLog;
		int id;

		id = super.getRequest().getData("id", int.class);
		activityLog = this.repository.findActivityLogById(id);

		super.getBuffer().addData(activityLog);
	}

	@Override
	public void bind(final ActivityLog activityLog) {
		assert activityLog != null;

		super.bindObject(activityLog, "registeredAt", "incidentType", "description", "severityLevel");
	}

	@Override
	public void validate(final ActivityLog activityLog) {
		assert activityLog != null;

		// Get original activity log from database
		ActivityLog original = this.repository.findActivityLogById(activityLog.getId());

		// Check if fields have changed
		if (original != null) {
			boolean hasChanged = false;

			// Check incidentType
			if (activityLog.getIncidentType() != null) {
				if (!activityLog.getIncidentType().equals(original.getIncidentType()))
					hasChanged = true;
			} else if (original.getIncidentType() != null)
				hasChanged = true;

			String currentDesc = activityLog.getDescription() == null || activityLog.getDescription().isEmpty() ? null : activityLog.getDescription();

			String originalDesc = original.getDescription() == null || original.getDescription().isEmpty() ? null : original.getDescription();

			if (!Objects.equals(currentDesc, originalDesc))
				hasChanged = true;

			// Check severityLevel
			if (activityLog.getSeverityLevel() != null) {
				if (!activityLog.getSeverityLevel().equals(original.getSeverityLevel()))
					hasChanged = true;
			} else if (original.getSeverityLevel() != null)
				hasChanged = true;

			super.state(!hasChanged, "*", "member.activitylog.form.error.readonly");
		}

	}

	@Override
	public void perform(final ActivityLog activityLog) {
		assert activityLog != null;

		activityLog.setDraftMode(false);
		this.repository.save(activityLog);
	}

	@Override
	public void unbind(final ActivityLog activityLog) {
		assert activityLog != null;

		Dataset dataset;

		dataset = super.unbindObject(activityLog, "registeredAt", "incidentType", "description", "severityLevel", "draftMode");
		dataset.put("masterId", activityLog.getAssignment().getId());
		//dataset.put("isDraftMode", activityLog.getAssignment().getIsDraftMode());

		super.getResponse().addData(dataset);
	}

}
