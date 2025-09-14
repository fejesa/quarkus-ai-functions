# Tools and Function Calling in the Context of LLMs

## Intro
Tools and function calling let LLMs move beyond pure text generation by interacting with external systems. With this approach, models can perform actions, fetch real-time information, and orchestrate multi-step tasks. The key benefits include extended capabilities, real-world execution, structured data generation for downstream processes, and the ability to build more dynamic, intelligent, and interactive agents.

A tool can be **anything**: a web search, an API call, or the execution of a specific piece of code.

It’s important to note that LLMs cannot execute tools directly. Instead, they **express the intent** to call a tool in their response (rather than replying with plain text). The actual execution happens in the application, which runs the tool with the provided arguments and then passes the result back to the model.

[LangChain4j](https://github.com/langchain4j/langchain4j) is a Java library that simplifies working with AI models through unified APIs. It supports multiple backends and provides utilities for building more advanced use cases on top of the basics.

For me, this project was an **experiment**: I wanted to see what happens if, instead of hardcoding business logic in Java, I let the **LLM orchestrate the logic** by calling tools. Could it really make decisions, iterate, and update structured data like a real program would?

That led me to explore, using **LangChain4j, [Quarkus](https://quarkus.io/), and [Ollama](https://ollama.com/)**, how LLMs handle:
* consuming, processing, and generating complex data structures,
* working around the lack of polymorphism in LangChain4j,
* manipulating the flow of execution, so the program becomes more dynamic and adaptable.

Think of it like classic control structures in programming — but driven by natural language instead of code:
* Selection → decisions and branching (like an `if` statement),
* Repetition → looping, i.e., repeating a step multiple times (like a `for` loop).
