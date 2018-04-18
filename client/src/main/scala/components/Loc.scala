package components

sealed trait Loc

sealed trait AuthLoc extends Loc

sealed trait AppLoc extends Loc

case object SignInLoc extends AuthLoc

case object HomeLoc extends AppLoc

case object TestLoc extends AppLoc