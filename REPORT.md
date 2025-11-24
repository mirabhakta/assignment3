Project Report
Challenges we faced:
Challenge 1: API setup struggles
Problem: API requests kept failing due to incorrect URL/headers and missing API key.
Solution: Reading Google’s documentation carefully fixed the bass URL, set auth header, and loaded the key correctly.
Learned: Follow the documentation more carefully, and double check API configuration to make sure it’s correctly formatted.

Challenge 2: Syntax Errors
Problem: Syntax errors often lead to problems we couldn’t figure out.
Solution: Used our debugging print statements and checks to figure out where the code went wrong.
Learned: Debugging print statements are crucial for tracking down syntax errors, especially when configuring an API.


Design Pattern Justifications
Strategy Pattern: Used different writing strategies such as creative, professional, and academic.
Factory Pattern: Used in RequestFactory.build(...) to create WritingRequest objects.
Observer Pattern: The controller listens for results from the API and then updates the view based off the different writing strategy.


AI Usage
We used ChatGpt at times to help with the different problems we were encountering with our code.
Asked: “What should my base URL and model name look like for the Gemini 2.5 Flash API in a Java client?“
Modified: We were getting a name format error, so by asking ChatGpt this question, we realized we had improperly formatted the Gemini model name.

Time spent: About 48 hours so faror
