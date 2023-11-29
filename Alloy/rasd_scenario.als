open util/boolean

abstract sig User {}

enum Status { Open, Closed }

sig Student extends User {
	var badges: set Badge,
	var score: one Int,
	var elegibleForBadge: Bool
} {
	score >= 0
}

sig Educator extends User {
	owns: disj set Tournament,
	hasPermissionsFor: set Tournament 
}

sig Battle {
	groupsMinSize: one Int,
	groupsMaxSize: one Int,
	requiresManualEvaluation: Bool,
	var status: one Status,
	enrolledGroups: disj set Group
} {
	groupsMinSize > 0 and 
	groupsMaxSize >= groupsMinSize and 
	groupsMaxSize <= 2 // for simulation purposes to limit number of signatures
}

sig Tournament {
	hosts: disj set Battle,
	hasBadges: disj set Badge, 
	var status: one Status 
}

sig Badge {}

var sig Solution {
	var evaluated: Bool,
	var evaluatedBy: lone Educator
}

sig Group {
	owner: one Student,
	members: some Student,
	var battleScore: one Int,
	var uploadedSolution: disj lone Solution
} {
	battleScore >= 0
}

// Every tournament has an owner 
fact allTournamentsHaveOwner {
	all t : Tournament | one e : Educator | t in e.owns
}

// Ownership of a tournament implies having permissions for that tournament
fact ownershipImpliesPermissionsForTournament {
	all e : Educator | e.owns in e.hasPermissionsFor 
}

// Ownership of a group implies being in that group
fact ownershipImpliesBeignMemberOfGroup {
	all s : Student | all g : Group | s in g.owner implies s in g.members
}

// Groups must be of correct size to enroll into battle 
fact correctGroupSize {
	all g : Group | let b = getBattleByGroup[g] |
		#g.members >= b.groupsMinSize and #g.members <= b.groupsMaxSize
}

// Student cannot have badges for tournaments that are still open
fact noOpenTournamentBadges {
	all s : Student | no t : Tournament | some b : Badge |
		b in s.badges and b in t.hasBadges and t.status = Open
}

// A battle must belong to one and only one tournament 
fact battleInTournament {
	all b : Battle | some t : Tournament | b in t.hosts
}

// A badge must belong to one and only one tournament
fact badgeInTournament {
 	all bg : Badge | some t : Tournament | bg in t.hasBadges
}

// Solutions must be created by a group
fact solutionsAreCreatedByGroups {
	always all s : Solution | some g : Group | s in g.uploadedSolution
}

// Students cannot participate to the same battle in different groups 
fact preventStudentsInMultipleGroupsForSameBattle {
	all disj g1, g2 : Group | (g1.members & g2.members) != none iff 
		getBattleByGroup[g1] != getBattleByGroup[g2]
}

fun getBattleByGroup[g: Group]: set Battle {
	enrolledGroups.g
}

// Once a tournament has been closed, it cannot be reopened
fact closedTournamentCannotBeReopened {
	always all t : Tournament | t.status = Closed implies t.status' = Closed
}

// Once a battle has been closed, it cannot be reopened
fact closedBattleCannotBeReopened {
	always all b : Battle| b.status = Closed implies b.status' = Closed
}

// If the status of a tournament goes from open to close, then closeTournament
// must have been called 
fact tournamentStatusChange {
	all t : Tournament | t.status = Open and t.status' = Closed implies
		closeTournament[t]
}

fact badgesAssignedAfterTournamentEnd {
	all t : Tournament | after closeTournament[t] implies (
		let s = getTournamentStudents[t] | s.elegibleForBadge = True implies
			some b : Badge | b in t.hasBadges and after assignBadge[s, b]
			implies b in s.badges'
	)
}

// Students cannot loose badges 
fact badgesCannotBeLost {
	always all s : Student | #s.badges' >= #s.badges
}

fact badgesAssignedOnlyIfElegible {
	all s : Student | some b : Badge | b not in s.badges and b in s.badges' iff 
		after assignBadge[s, b] and s.elegibleForBadge = True 
}

fun getTournamentStudents[t : Tournament]: set Student {
	((t.hosts).enrolledGroups).members
}

pred closeTournament[t : Tournament] {
	t.status' = Closed 
}

pred assignBadge[s : Student, bg : Badge] {
	s.badges' = s.badges + bg
}

// After having uploaded a solution, the group must always upload another
fact uploadedSolutionInTime {
	always all g : Group | g.uploadedSolution != none implies 
		g.uploadedSolution' != none
}

// If a group hasn't uploaded a solution, then they should have 0 points
fact noSolutionZeroScore {
	always all g : Group | #(g.uploadedSolution) = 0 implies g.battleScore = 0
}

// If a tournament is closed, then all battles within the tournament should be closed
fact noOpenBattlesInClosedTournament {
	always all t : Tournament | let b = t.hosts | 
	 	t.status = Closed implies b.status = Closed
}

// End of a battle and evaluation
pred endBattle[b: Battle] {
	b.status' = Closed
}

pred giveManualEvaluation[e: Educator, s: Solution] {
	s.evaluated = True
	s.evaluatedBy = e
} 

// Once a solution has been evaluated it should remain as such
fact persistentEvaluatedSolution {
	always all s : Solution | s.evaluated = True implies s.evaluated' = True and
		s.evaluatedBy != none implies s.evaluatedBy = s.evaluatedBy'
}

// Only solutions within battles with the option for manual evaluation should have one
fact manualEvaluationOnlyForBattlesThatRequireIt {
	always all b : Battle | let s = getBattleSolutions[b] | s.evaluatedBy != none implies 
		b.requiresManualEvaluation = True
}

fun getBattleSolutions[b: Battle]: set Solution {
	(b.enrolledGroups).uploadedSolution
}

// Solutions in closed battles should be evaluated
pred evaluatedSolutions {
	always all b : Battle | let s = getBattleSolutions[b] | #s > 0 and b.status = Closed implies
		 s.evaluated = True 
}

// Solutions in closed battles that require manual evaluations should have one 
pred requireManualEvaluation {
	always all b : Battle | let s = getBattleSolutions[b] | b.status = Closed and 
		b.requiresManualEvaluation = True implies s.evaluatedBy != none
}

fact endOfBattleRequiresEvaluation {
	always all b : Battle | let s = getBattleSolutions[b] | 
		b.status = Open and b.status' = Closed implies 
			s.evaluated' = True and 
			(b.requiresManualEvaluation = True implies s.evaluatedBy' != none)	
}TO ALLOW FOR THE VIEWVING OF THE EDIT HISTORY CANNOT BE VIEWED

// TODO: continue with evaluation and badges

// TODO: change parameters of simulation	
pred show [t: Tournament, b: Battle] {
	t.status = Open; t.status = Open; t.status = Closed
	b.status = Open; b.status = Open
	b.requiresManualEvaluation = True
	#(Group.uploadedSolution) < 1; #(Group.uploadedSolution) > 0; #(Group.uploadedSolution) >= #Group 
	#(Student.badges) < 2 and #(Student.badges) > 0; #(Student.badges) > 3
	#Battle = 2
	#Tournament = 3
	#Solution = 0; #Solution > 1; #Solution >= #Group
	#Group >= #Battle
}

run show for 10 but 3 Tournament, 3 steps

