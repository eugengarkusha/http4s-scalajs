package components

import java.util.UUID

sealed trait Loc

sealed trait AuthLoc extends Loc

sealed trait AppLoc extends Loc

case class SignInLoc(activationId: Option[UUID] = None) extends AuthLoc

case object SignUpLoc extends AuthLoc

case object HomeLoc extends AppLoc

case object TestLoc extends AppLoc
