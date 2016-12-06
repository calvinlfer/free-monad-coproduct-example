import cats.free.Free
import cats.{Id, ~>}
import freek._

// Credits: https://github.com/ProjectSeptemberInc/freek
object Example extends App {
  sealed trait LogInstruction[Result]
  case class Debug(message: String) extends LogInstruction[Unit]
  case class Info(message: String) extends LogInstruction[Unit]
  case class Warn(message: String) extends LogInstruction[Unit]

  sealed trait GreetingsInstruction[Result]
  case class WhoAreYou(message: String) extends GreetingsInstruction[String]
  case object Hello extends GreetingsInstruction[Unit]
  case object Bye extends GreetingsInstruction[Unit]

  type ApplicationInstruction = LogInstruction :|: GreetingsInstruction :|: NilDSL
  val ApplicationInstruction = DSL.Make[ApplicationInstruction]

  // smart constructors
  def debug(message: String): Free[ApplicationInstruction.Cop, Unit] =
    Debug(message).freek[ApplicationInstruction]

  def info(message: String): Free[ApplicationInstruction.Cop, Unit] =
    Info(message).freek[ApplicationInstruction]

  def warn(message: String): Free[ApplicationInstruction.Cop, Unit] =
    Warn(message).freek[ApplicationInstruction]

  def whoAreYou(message: String): Free[ApplicationInstruction.Cop, String] =
    WhoAreYou(message).freek[ApplicationInstruction]

  def hello: Free[ApplicationInstruction.Cop, Unit] =
    Hello.freek[ApplicationInstruction]

  def bye: Free[ApplicationInstruction.Cop, Unit] =
    Bye.freek[ApplicationInstruction]

  // Log interpreter implementation
  val logInterpreter = new (LogInstruction ~> Id) {
    override def apply[A](fa: LogInstruction[A]): Id[A] = fa match {
      case Debug(message) =>
        println(s"DEBUG: $message")
        ()
      case Warn(message) =>
        println(s"WARN: $message")
        ()
      case Info(message) =>
        println(s"INFO: $message")
        ()
    }
  }

  val greetingsInterpreters = new (GreetingsInstruction ~> Id) {
    override def apply[A](fa: GreetingsInstruction[A]): Id[A] = fa match {
      case WhoAreYou(message: String) =>
        println(message)
        val userInput = scala.io.StdIn.readLine()
        userInput

      case Hello =>
        println("Hello!")
        ()

      case Bye =>
        println("Bye!")
        ()
    }
  }

  // Example program (Pure)
  val instructions = for {
    _   <- debug("beginning program")
    _   <- hello
    you <- whoAreYou("Enter your name")
    _   <- info(s"Hello $you")
    _   <- warn("Exiting program")
  } yield ()


  // Combine interpreters
  val composedInterpreter = logInterpreter :&: greetingsInterpreters

  // Run instructions (that were composed together from different instruction sets) through composed interpreter
  // Side effecting - pushed to the end of the program
  instructions.interpret(composedInterpreter)
}
