open util/boolean

abstract sig User {}

enum Status { Open, Closed }

sig Student extends User {
	var awardedBadges: set Badge,
	// var score: one Int,
	var satisfiedBadges: set Badge
} {
	// score >= 0
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
	battles: disj set Battle,
	badges: disj set Badge, 
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
	// var battleScore: one Int,
	var currentSolution: disj lone Solution
} {
	// battleScore >= 0
}