
package acme.features.member.assignment;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.assignment.Assignment;
import acme.entities.assignment.AssignmentStatus;
import acme.entities.assignment.Role;
import acme.entities.flightcrewmember.AvailabilityStatus;
import acme.realms.Member;

@GuiService
public class MemberAssignmentDeleteService extends AbstractGuiService<Member, Assignment> {

	@Autowired
	private MemberAssignmentRepository repository;


	@Override
	public void authorise() {
		boolean status;
		int assignmentId;
		Assignment assignment;
		int memberId;

		assignmentId = super.getRequest().getData("id", int.class);
		assignment = this.repository.findOneById(assignmentId);
		memberId = super.getRequest().getPrincipal().getActiveRealm().getId();

		status = assignment != null && assignment.getDraftMode() && assignment.getMember().getAvailabilityStatus() == AvailabilityStatus.AVAILABLE && super.getRequest().getPrincipal().hasRealmOfType(Member.class)
			&& assignment.getMember().getId() == memberId;

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		Assignment assignment;
		int id;

		id = super.getRequest().getData("id", int.class);
		assignment = this.repository.findOneById(id);

		super.getBuffer().addData(assignment);
	}

	@Override
	public void bind(final Assignment assignment) {
		assert assignment != null;

		super.bindObject(assignment, "role", "status", "remarks", "leg");
	}

	@Override
	public void validate(final Assignment assignment) {
		assert assignment != null;

	}

	@Override
	public void perform(final Assignment assignment) {
		assert assignment != null;

		this.repository.delete(assignment);

	}

	@Override
	public void unbind(final Assignment assignment) {
		assert assignment != null;

		int currentMemberId = super.getRequest().getPrincipal().getActiveRealm().getId();
		Member member = this.repository.findMemberById(currentMemberId);

		SelectChoices statusChoices = SelectChoices.from(AssignmentStatus.class, assignment.getStatus());
		SelectChoices roleChoices = SelectChoices.from(Role.class, assignment.getRole());
		SelectChoices legChoices;
		legChoices = SelectChoices.from(this.repository.findAllPFL(MomentHelper.getCurrentMoment(), member.getAirline().getId()), "flightNumber", assignment.getLeg());

		Dataset dataset = super.unbindObject(assignment, "role", "lastUpdate", "status", "remarks", "draftMode");

		dataset.put("role", roleChoices);
		dataset.put("status", statusChoices);

		dataset.put("leg", legChoices.getSelected().getKey());
		dataset.put("legs", legChoices);
		dataset.put("member", member.getEmployeeCode());

		super.getResponse().addData(dataset);
	}

}
