package components

sealed trait Loc

case object SignInLoc extends Loc

case object HomeLoc extends Loc

case object TestLoc extends Loc