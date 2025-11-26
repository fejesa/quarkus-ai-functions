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
* Selection:  decisions and branching (like an `if` statement),
* Repetition: looping, i.e., repeating a step multiple times (like a `for` loop).

## The Problem I Wanted to Solve
Systems integration is not a new topic — we often need to access or provide data to other systems.

Let’s say our system — called **Institute** — stores data about users (names, addresses) provided during registration. The Institute needs to extend this information with a digital or external ID that belongs to another system — called **Statistic**.

The Statistic system also stores user data (names, addresses), but because it is a different system, the following scenarios can occur:
* the given user is not found in Statistic,
* there is an exact match for the given user in both systems,
* multiple hits are found based on the filtering criteria.

I used **first name, last name, and birth date** of the user as filtering criteria.
* In case of an exact match, Statistic sent back the external ID with the address data as well.
* But in case of multiple hits, Statistic sent back all records without the external ID!

So how can Institute decide which user record best matches?

Here I used the [Jaro–Winkler Distance](https://en.wikipedia.org/wiki/Jaro%E2%80%93Winkler_distance), a similarity measure that quantifies the difference between two strings, often used in record linkage, data deduplication, and string matching. I calculated similarity scores between addresses, and the response contained these scores for each record.

But the story did not stop there! I also wanted the LLM to **generate a detailed explanation** in the response about the exact differences between the records.

(By the way: I suspected I was asking too much from the LLM. But honestly, that was the main goal of this experiment 🙂).

### Example Outputs
Case 1: No user found in Statistic
```json
{
  "type": "NONEMATCH",
  "person": {
    "birthDate": "1990-05-21",
    "firstName": "Alice",
    "lastName": "Johnson"
  }
}
```

Case 2: Exact match found
```json
{
    "type": "EXACTMATCH",
    "user": {
        "externalId": "EXT-1001",
        "address": {
            "city": "Hamburg",
            "country": "Germany",
            "houseNumber": "10",
            "street": "Sample Str.",
            "zipCode": "20095"
        },
        "person": {
            "birthDate": "2000-07-21",
            "firstName": "Clara",
            "lastName": "Meier"
        }
    }
}
```

Case 3: Multiple candidate matches where 4 users were found
```json
{
    "type": "SIMILARMATCH",
    "users": [
        {
            "address": {
                "city": "Munich",
                "country": "Germany",
                "houseNumber": "56",
                "street": "Sendlinger Strasse",
                "zipCode": "80332"
            },
            "person": {
                "birthDate": "1982-04-08",
                "firstName": "Peter",
                "lastName": "Weber"
            },
            "explanation": "country matches, city matches, zipCode differs, street matches, houseNumber differs",
            "score": 0.968421052631579
        },
        {
            "address": {
                "city": "Munich",
                "country": "Germany",
                "houseNumber": "12A",
                "street": "Sendlinger Straße",
                "zipCode": "80331"
            },
            "person": {
                "birthDate": "1982-04-08",
                "firstName": "Peter",
                "lastName": "Weber"
            },
            "explanation": "country matches, city matches, zipCode matches, street differs, houseNumber differs",
            "score": 0.974089068825911
        },
        {
            "address": {
                "city": "Augsburg",
                "country": "Germany",
                "houseNumber": "11",
                "street": "Sendlinger Strasse",
                "zipCode": "80331"
            },
            "person": {
                "birthDate": "1982-04-08",
                "firstName": "Peter",
                "lastName": "Weber"
            },
            "explanation": "country matches, city differs, zipCode matches, street matches, houseNumber differs",
            "score": 0.853641765704584
        },
        {
            "address": {
                "city": "Salzburg",
                "country": "Austria",
                "houseNumber": "12",
                "street": "Sendlinger Strasse",
                "zipCode": "80331"
            },
            "person": {
                "birthDate": "1982-04-08",
                "firstName": "Peter",
                "lastName": "Weber"
            },
            "explanation": "country differs, city matches, zipCode matches, street matches, houseNumber matches",
            "score": 0.6634711779448621
        }
    ]
}
```

### My Constraints
* Must run locally (privacy and cost reasons).
* Should work on my laptop (32 GB RAM — not huge for LLMs).
* Prefer open-source tools.

## Technology Choices
I opted for:
* LangChain4j: Open, composable framework with a standard interface for models, tools, and databases. Perfect for building an LLM workflow in Java.
* Ollama: Runs locally, easy to use, and provides solid tooling support.
* Quarkus: Seamless integration with LangChain4j. Registering an AI service only requires annotations. Exposing tools is straightforward (just annotate your Java code). And not a small thing — developing with Quarkus is genuinely fun.

**Note:** I used Ollama as the LLM backend, which runs models locally. I tried several models, but for this experiment, I settled on `llama3-8b`, which provided a good balance between performance and quality.

Now let’s walk through each of the topics I mentioned in the introduction.

## Data Structures and Polymorphism
Polymorphism brings flexibility, reusability, and maintainability. I defined the Statistic API like this:
```java
UserSearchResult searchUser(String firstName, String lastName, String birthDate);
```
where `UserSearchResult` can represent different result types:
```java
sealed interface UserSearchResult permits NoMatchResult, SimilarMatchesResult, ExactMatchResult {}
record NoMatchResult(Person person) implements UserSearchResult {}
record ExactMatchResult(MatchUser user) implements UserSearchResult {}
record SimilarMatchesResult(List<MatchUser> users) implements UserSearchResult {}
```

And `MatchUser` looked like this:
```java
record MatchUser(Person person, Address address, Double score, String explanation, String externalId) {}
```

The full type hierarchy is shown below:

![Type Hierarchy](docs/output-types-diagram.png)

This design leveraged **sealed types** and **records** in Java, giving me safe and concise code.

But here’s the bad news: **LangChain4j does not support polymorphism.**
The LLM can consume and generate structured JSON, but it does not know which subtype a given JSON represents without a schema.

**Good news**: In LangChain4j, JSON schema is automatically generated from POJOs in Quarkus/Spring Boot. You can enhance them with `@Description`. Example:
```java
@Description("The user's personal information, for example, first name, last name, and birth date")
record Person(
    @Description("The user's first name") String firstName,
    @Description("The user's last name") String lastName,
    @Description("The user's birth date") String birthDate
) {}
```

But that still does not solve polymorphism. For that, I used [Jackson](https://github.com/FasterXML/jackson) java library annotations to help with serialization and deserialization.:
```java
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "type")
sealed interface UserSearchResult permits NoMatchResult, SimilarMatchesResult, ExactMatchResult {}
```

With subtype markers:
```java
@UserSearchResultSubType("NONEMATCH")
record NoMatchResult(Person person) implements UserSearchResult {}
```

The "type" field is injected into JSON for the LLM, but is not part of the Java hierarchy (see the example outputs above).

## Flow of Execution
I did not want to implement the business logic in Java code. Instead, I only exposed the tools/functions that can be used by the LLM. But how can the application provide the expected output without a single line of business logic? This is where **system prompts** come into play! Instead of writing imperative code, I wrote a detailed description of the business logic in natural language, which guided the LLM in orchestrating the tools.

The following diagram shows the main components of the application and their relationships.

![Architecture](docs/component-diagram.png)

where the `UserSearchAssistant` is registered as an `AiService`, the other components are the tools, and LangChain4j handles the orchestration.

I defined the following tools:
* `StatisticUserService#searchUser(String firstName, String lastName, String birthDate)` Searches users in the Statistic database based on the provided filtering criteria. In this project, the service also runs within the same Java application, but in the real world it should be a remote service accessed via REST.
* `InstituteUserService#getUserAddress(Person person)` Retrieves the user’s address from the Institute system.
* `SimilarityDistanceCalculator#jaroWinklerSimilarity(Address original, Address similar)` Computes similarity scores between two addresses using the Jaro–Winkler distance algorithm.

**Note:** Some model can calculate similarity internally, but I wanted to experiment with tool calling.

Each tool has different parameter types that must be constructed properly by the model in order to be called. This is one of the reasons why **accurate JSON schema definitions** are so critical when working with LLMs.

Now let’s see how the sequence diagram looks in the case where similar users are found:

![Sequence Diagram](docs/flow-of-execution-diagram.png)

* The **red arrows** represent the REST calls to the LLM. These calls contain: which tool can be called/used, with which parameters, and in some cases, also the results of tool executions that should be taken into account by the model.
* The **blue arrows** are the responses sent back by the LLM. These contain the instructions for which tool should be executed, along with the provided parameters.
* The **green arrows** represent the actual tool executions performed by the application.

Here’s how the flow looks in detail:
* After we send the extended prompt (with tool descriptions) to the model, it **calls the Statistic API** to search for the user.
* The model analyzes the response and decides whether the case is a `SIMILARMATCH`.
* If it is, the model calls the **getUserAddress** tool to fetch the Institute user’s address, which will serve as the baseline for similarity calculations.
* Next, the model **ITERATES over the list of candidate addresses** and calls the **jaroWinklerSimilarity** tool for each candidate. (For simplicity, the diagram only shows two iterations.)
* After obtaining similarity scores, the model **updates the score fields** in the JSON.
* Finally, the model goes through each address field—country, city, zip code, street, house number—and **generates an explanation of the differences**, inserting them into the explanation field of the JSON.

Wow! It looks like a fairly complex workflow, but if you were to implement it in code, it would be quite straightforward. For the LLM, however, this orchestration is much more fragile and error-prone.

But how the LLM tool access request looks like? Here’s an example response body from the LLM when it wants to call the `jaroWinklerSimilarity` function:
```json
{
  "model": "llama3.1",
  "created_at": "2025-09-12T15:17:17.613184Z",
  "message": {
    "role": "assistant",
    "content": "",
    "tool_calls": [
      {
        "function": {
          "name": "jaroWinklerSimilarity",
          "arguments": {
            "original": {
              "city": "Munich",
              "country": "Germany",
              "houseNumber": "48",
              "street": "Sendlinger Strasse",
              "zipCode": "80331"
            },
            "similar": {
              "city": "Salzburg",
              "country": "Austria",
              "houseNumber": "12",
              "street": "Sendlinger Strasse",
              "zipCode": "80331"
            }
          }
        }
      }
    ]
  },
  "done_reason": "stop",
  "done": true,
  "total_duration": 2281647584,
  "load_duration": 53564417,
  "prompt_eval_count": 2519,
  "prompt_eval_duration": 115408667,
  "eval_count": 78,
  "eval_duration": 2101600166
}
```

The response is parsed by LangChain4j, which extracts the tool call request and executes the function with the provided arguments.

## Challenges with System Prompts and Tool Use
Writing a system prompt for this context turned out to be really challenging. Even small changes — like using similar words or synonyms — could confuse the model. From a programmer’s perspective, this business use case seems simple. But when you delegate the interpretation and execution of business logic to an LLM, things are not that straightforward.

One prompt might be executed correctly by one model, but another model could completely misinterpret it. In fact, I found that **clear, explicit instructions for the model are even more important than writing a very detailed requirements specification for a developer**.

And here’s the tricky part: any change in the prompt needs to be documented and tested carefully. Sometimes I only wanted to polish the prompt to improve the output quality, but the result ended up being the exact opposite!

Below are some of the main issues I encountered during prompt engineering. (The list is not complete!)

**Missing** `birthDate` **during deserialization**
* **Issue**: Jackson failed with Missing required creator property 'birthDate' when deserializing a Person record.
* **Category**: Schema / Validation mismatch
* **Cause**: The JSON didn’t always contain birthDate, but the record field was annotated with `@JsonProperty(required = true)`.
* **Solution**: Enforce birthDate as mandatory both in the schema and during input validation. Keep `@JsonProperty(required = true)` and fail early if the field is missing.

**Model repeatedly calling the same tool**
* **Issue**: The model retried the same function call (e.g., `searchUser`) multiple times.
* **Category**: Looping / Retry hallucination
* **Cause**: LLM uncertainty led to uncontrolled retries.
* **Solution**: Add a strict prompt rule: _“Do not repeat tool calls with the same input.”_

**Core identity fields were modified**
* **Issue**: In `SIMILARMATCH`, the model sometimes changed `firstName`, `lastName`, or `birthDate`.
* **Category**: Hallucination / Data substitution
* **Cause**: The LLM attempted to normalize or “correct” identifiers.
* **Solution**: Add explicit binding rules: _“firstName, lastName, and birthDate MUST NEVER be changed, corrected, reformatted, guessed, or substituted.”_ Only address fields are mutable during similarity checks.

**Unnecessary tool recursions**
* **Issue**: The model re-called `getUserAddress` or `jaroWinklerSimilarity` multiple times unnecessarily.
* **Category**: Over-generation / Looping
* **Cause**: The LLM lacked awareness of workflow completion.
* **Solution**: Enforce strict workflow rules in the prompt:
  * Call `getUserAddress` exactly once.
  * Compare each candidate with `jaroWinklerSimilarity`.
  * Aggregate and return results without re-calling tools.

**Candidate address replaced by original address**
* **Issue**: In the output, a candidate’s address was sometimes replaced by the original user’s address.
* **Category**: Data leakage / Confusion error
* **Cause**: The model confused the baseline (original address) with the candidates.
* **Solution**: Add a rule to the prompt: _“The original address is ONLY a baseline for similarity. NEVER use it as a candidate in results.”_

**JSON formatting errors**
* **Issue**: The model returned objects as stringified JSON (`"{…}"`) or with incorrect field names.
* **Category**: Schema conformance failure
* **Cause**: The LLM confused JSON objects with strings and sometimes invented field names.
* **Solution**:
  * Add prompt guidance: _“Always pass tool parameters as JSON OBJECTS, never strings.”_
  * Enforce exact field names: `country`, `city`, `zipCode`, `street`, `houseNumber`.

And many, many more! These problems really showed me how fragile prompt engineering can be. What looks like a minor detail for a human developer can completely derail the model. It sometimes felt like debugging a black box that insists on being creative.

**Note:** The working system prompts are not optimal. I’m sure they can be improved further. But for now, they work well enough to demonstrate the concept.

## Final Thoughts
The most challenging part of this project was **writing system prompts**. Any small change could break the whole process: hallucinations, malformed JSON, infinite loops.
I realized:
* Giving too many tools increases confusion and looping.
* Delegating too much business logic to the LLM makes prompts fragile.
* A safer design is decomposition: break the task into smaller steps with validators in between.

## Installation
### Prerequisites
- JDK 21 or higher
- Maven 3.5+
- Docker
- [Ollama](https://ollama.com) installed
- [httpie](https://httpie.io/) installed (optional, but useful for testing)

### Steps
1. Clone the repository:
   ```sh
   git clone https://github.com/fejesa/quarkus-ai-functions.git
    ```
2. Build the project:
   ```sh
   mvn clean install
   ```
3. Run the application in development mode:
   ```sh
    mvn quarkus:dev
    ```
**Note**: No need to run neither the Ollama nor the PostgreSQL manually — Quarkus Dev mode will automatically start them for you. If the LLM models are not available locally, Quarkus will download them automatically, but this may take some time.

## Configuration
The application can be configured via `application.properties`. The following properties are available:
```properties
quarkus.http.port = 8080
# PostgreSQL database automatically created by Dev Services with the following settings
quarkus.devservices.enabled = true
quarkus.datasource.devservices.port = 5434
quarkus.datasource.devservices.db-name = quarkus
quarkus.datasource.devservices.username = quarkus
quarkus.datasource.devservices.password = quarkus

quarkus.hibernate-orm.schema-management.strategy = drop-and-create
quarkus.hibernate-orm.log.sql = true

quarkus.langchain4j.log-requests = true
quarkus.langchain4j.log-responses = true
# The temperature to use for the chat model. Temperature is a value between 0 and 1, where lower values make the model more deterministic and higher values make it more creative.
quarkus.langchain4j.temperature = 0.1
# Global timeout for requests to LLM APIs
quarkus.langchain4j.timeout = 120s
quarkus.langchain4j.guardrails.max-retries = 2

# The chat model to use. In case of Ollama, llama3.1 is the default chat model.
quarkus.langchain4j.ollama.chat-model.model-id = llama3.1
# The format to return a response in. Format can be json or a JSON schema, or text; in this application, we use JSON.
quarkus.langchain4j.ollama.chat-model.format = JSON

# The REST Assured client timeout for testing.
quarkus.http.test-timeout = 60s

# LangFuse OpenTelemetry settings; set to false to disable
quarkus.otel.enabled = true
quarkus.otel.metrics.enabled = true
# OpenTelemetry defines the encoding of telemetry data and the protocol used to exchange data between the client and the server. Default is grpc.
quarkus.otel.exporter.otlp.traces.protocol = http/protobuf
# LangFuse OpenTelemetry endpoint and authorization header
quarkus.otel.exporter.otlp.traces.headers = Authorization=Basic ***
quarkus.otel.exporter.otlp.traces.endpoint = http://localhost:3000/api/public/otel
# quarkus.otel.exporter.otlp.logs.protocol = http/protobuf
quarkus.langchain4j.tracing.include-prompt = true
quarkus.langchain4j.tracing.include-completion = true
quarkus.langchain4j.tracing.include-tool-arguments=true
quarkus.langchain4j.tracing.include-tool-result=true

# Grafana and OpenTelemetry ports for LGTM observability; by default testcontainers exposes these on random ports.
quarkus.observability.lgtm.grafana-port = 3001
quarkus.observability.lgtm.otel-grpc-port = 5317
quarkus.observability.lgtm.otel-http-port = 5318
# Grafana LGTM metrics
quarkus.otel.exporter.otlp.metrics.endpoint = http://localhost:5318
quarkus.otel.exporter.otlp.metrics.protocol = http/protobuf
#Grafana LGTM logs
quarkus.otel.exporter.otlp.logs.endpoint = http://localhost:5318
quarkus.otel.exporter.otlp.logs.protocol = http/protobuf
```

The sample data - defined in the `import.sql` - is automatically loaded into the database when the application starts.

## Observability, Monitoring & Tracing
- Observability is essential — it gives us visibility into what happens under the hood when our application runs, enabling reliable debugging, performance tuning, and root-cause analysis.
- We rely on the “three pillars” of observability: **metrics** (system performance and health), **logs** (event history and context), and **traces** (detailed journeys of requests and LLM interactions).
    - Metrics enable us to monitor throughput, latency, error rates, resource consumption — and alert on anomalies.
    - Logs provide detailed context: timestamps, errors, stack traces, user actions, state changes — essential for forensic debugging and audit trails.
    - Traces reconstruct the full flow of a request (or an LLM call), showing each step, latency, dependencies, and where things can go wrong.
- For our LLM-based application, observability is even more critical: LLMs are non-deterministic, involve multiple subsystems (embedding, retrieval, generation, external APIs), and can fail or degrade silently.
- We use **[Langfuse](https://github.com/langfuse/langfuse)** for tracing LLM interactions: it captures prompts, embeddings, tool calls, completions, latencies, costs and more — giving us full visibility into the LLM pipeline.
    - Langfuse is open-source, self-hostable, and framework-agnostic — which makes it a robust, vendor-independent observability solution.
    - With Langfuse we can debug complex agent flows, trace unexpected outputs or failures to exact inputs, optimize prompt engineering, and monitor performance & cost per call.
    - Langfuse also supports evaluation workflows: tracking output quality over time, annotating traces, detecting regressions or drift — which helps maintain model reliability in production.
- Combining standard application metrics/logs (via [Grafana](https://grafana.com/) / Prometheus / Loki) with detailed LLM tracing (via Langfuse) gives us a **holistic observability stack**: infrastructure, app, and LLM.
- This observability stack makes our RAG application more maintainable, debuggable, auditable, and performant — especially important when serving real users in production.

To run Langfuse locally with Docker, you can use the following the instructions [here](https://github.com/langfuse/langfuse).

## Testing the Application
You can test the application using [httpie](https://httpie.io/) or [curl](https://curl.se/) or any REST client of your choice.
### Example Requests
No match found:
```sh
http -v localhost:8080/users firstName==Alice lastName==Johnson birthDate==1990-05-21
```

Exact match found:
```sh
 http -v localhost:8080/users firstName==Clara lastName==Meier birthDate==2000-07-21
```

Multiple similar matches found:
```sh
http -v localhost:8080/users firstName==Peter lastName==Weber birthDate==1982-04-08
```

**Note:** It takes some time for the model to respond, and it can also happen that the model needs to be downloaded first. So please be patient.

If you want to check the traces in Langfuse, make sure you have it running locally and configured properly in `application.properties`. You can then access the Langfuse UI at `http://localhost:3000`.
