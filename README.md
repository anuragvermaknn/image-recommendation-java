# image-recommendation-java



1. Build Using Maven : mvn clean compile assembly:single

2. Run (command line): java -cp  java-test-0.0.1-SNAPSHOT-jar-with-dependencies.jar com.sparkscientist.stickerrecommendation.Controller

3. Go to Url : http://localhost:9090/input (Try words like love, luv, movie, darling etc. Since the training data is a small one, you will not get recommendation for every word)

4. Port can be changed in Controller.java


Class StickerRecommenderV2.java has the methods for loading data, computing bayesian probabilities, inverse doc frequencies and the core algorithm. Please do check the [blogpost] (http://sparkscientist.com/index.php/2017/04/14/image-recommendation/) to understand the underlying intuition and concepts behind the implementation.

**TODO:**
1. Parallelize the computation processes for different words.
2. Using the [Word2Vector](https://deeplearning4j.org/word2vec), prepare a new dataset for Spanglish, Frenglish etc.

