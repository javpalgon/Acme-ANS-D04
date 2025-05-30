
package acme.features.member.activityLog;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.activitylog.ActivityLog;
import acme.entities.assignment.Assignment;
import acme.entities.assignment.AssignmentStatus;
import acme.entities.flightcrewmember.AvailabilityStatus;
import acme.entities.leg.LegStatus;
import acme.realms.Member;

@GuiService
public class MemberActivityLogListService extends AbstractGuiService<Member, ActivityLog> {

	@Autowired
	private MemberActivityLogRepository repository;


	@Override
	public void authorise() {
		boolean status;
		int masterId;
		Assignment assignment;
		int memberId;

		masterId = super.getRequest().getData("masterId", int.class);
		assignment = this.repository.findAssignmentById(masterId);
		memberId = super.getRequest().getPrincipal().getActiveRealm().getId();
		status = assignment != null;

		super.getResponse().setAuthorised(
			status && !assignment.getDraftMode() && assignment.getMember().getAvailabilityStatus().equals(AvailabilityStatus.AVAILABLE) && assignment.getMember().getId() == memberId && !assignment.getStatus().equals(AssignmentStatus.CANCELLED));
	}

	@Override
	public void load() {
		List<ActivityLog> activityLog;

		int masterId = super.getRequest().getData("masterId", int.class);
		int memberId = super.getRequest().getPrincipal().getActiveRealm().getId();

		activityLog = this.repository.findByMemberIdAndAssignmentId(memberId, masterId);

		super.getBuffer().addData(activityLog);
	}

	@Override
	public void unbind(final ActivityLog activityLog) {
		assert activityLog != null;

		Dataset dataset;

		dataset = super.unbindObject(activityLog, "registeredAt", "incidentType", "description", "severityLevel");

		super.addPayload(dataset, activityLog, "registeredAt", "incidentType");
		super.getResponse().addData(dataset);

	}

	@Override
	public void unbind(final Collection<ActivityLog> entities) {

		assert entities != null;

		int masterId = super.getRequest().getData("masterId", int.class);
		Assignment assignment = this.repository.findAssignmentById(masterId);
		final boolean showCreate = assignment.getLeg().getStatus().equals(LegStatus.LANDED) && !assignment.getStatus().equals(AssignmentStatus.CANCELLED);

		super.getResponse().addGlobal("showCreate", showCreate);
		super.getResponse().addGlobal("masterId", masterId);
	}

}
