# Free Monads with FreeK
A demonstration on how to compose different instruction sets together using `Coproduct` minimizing boilerplate using 
`FreeK`

Libraries used: 
- [Cats](http://typelevel.org/cats/datatypes/freemonad.html)
- [FreeK](https://github.com/ProjectSeptemberInc/freek)

In the example shown [here](https://github.com/calvinlfer/free-monads-functional-web-apps/commit/aa14d27dd390f5478b0b37e670a99d58210f5e5e#diff-df2bbb8433e5fb4f61e883e5b5cd8ca6R36)
which is based off Chris Myer's talk: [A Year Living Freely](https://www.youtube.com/watch?v=rK53C-xyPWw), we cheat and 
make instruction sets inherit from a common instruction set. When you do this, it becomes easy to compose instruction 
sets due to the common instruction set so you build a big interpreter that takes in the common instruction trait and 
dispatches instructions to the little interpreters. You can see an example of this in action [here](https://github.com/calvinlfer/free-monads-functional-web-apps).

If you don't have the luxury of controlling the source of all instruction sets then you need to turn to `Coproducts`.
Rúnar talks about this concept [here](http://functionaltalks.org/2014/11/23/runar-oli-bjarnason-free-monad/). This 
project is a demonstration similar to Rúnar's but using [FreeK](https://github.com/ProjectSeptemberInc/freek)'s work to 
minimize as much boilerplate as possible. 

We have two instruction sets:
- Logging
    ```scala
      sealed trait LogInstruction[Result]
      case class Debug(message: String) extends LogInstruction[Unit]
      case class Info(message: String) extends LogInstruction[Unit]
      case class Warn(message: String) extends LogInstruction[Unit]
    ```

- Greeting
    ```scala
      sealed trait GreetingsInstruction[Result]
      case class WhoAreYou(message: String) extends GreetingsInstruction[String]
      case object Hello extends GreetingsInstruction[Unit]
      case object Bye extends GreetingsInstruction[Unit]
    ```

We want to be able to compose instructions from both instruction sets together into a single program.

## Process
Define a `Coproduct` that mixes the instruction sets together with the help of FreeK
```scala
import freek._
  sealed trait LogInstruction[Result]
  // ...
  
  sealed trait GreetingsInstruction[Result]
  // ...
  
  type ApplicationInstruction = LogInstruction :|: GreetingsInstruction :|: NilDSL
  val ApplicationInstruction = DSL.Make[ApplicationInstruction]
```

Define smart constructors that lift both your instruction sets into the Coproduct instruction set using `Free`
```scala
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
```

You can write your interpreters for each instruction set as usual

- Logging 
    ```scala
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
    ```
    
- Greeting
    ```scala
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
    ```

You can write your program using instructions from both instruction sets using the smart constructors. Since they have 
been lifted into Free of the Coproduct, you can compose instructions from both instruction sets

```scala
val instructions = for {
_   <- debug("beginning program")
_   <- hello
you <- whoAreYou("Enter your name")
_   <- info(s"Hello $you")
_   <- warn("Exiting program")
} yield ()
```

These are just instructions that haven't been executed yet, they just describe what we would like to do. Let's look at 
how we can run these instructions through an interpreter that side-effects and executes these instructions. To do this 
we need to compose our little interpreters together:

```scala
// Combine interpreters
val composedInterpreter = logInterpreter :&: greetingsInterpreters
```

Now let's run the instructions that were composed together from different instruction sets through the composed interpreter
```scala
instructions.interpret(composedInterpreter)
```

