package com.example.mirixa

import android.util.Log
import com.google.firebase.database.FirebaseDatabase

object DatabaseSeeder {

    fun seedDatabase() {
        val database = FirebaseDatabase.getInstance("https://mirixa-b998b-default-rtdb.firebaseio.com").reference
        
        // 1. Categories
        val categories = mapOf(
            "1" to Category("1", "Programming Languages", 6, "code"),
            "2" to Category("2", "Android Development", 5, "android"),
            "3" to Category("3", "Web Development", 5, "web"),
            "4" to Category("4", "Artificial Intelligence", 5, "ai"),
            "5" to Category("5", "Database Management", 5, "database")
        )

        // 2. Courses
        val coursesMap = mutableMapOf<String, Course>()

        // CATEGORY 1: PROGRAMMING
        addCourse(coursesMap, "c1", "1", "Python Programming", "Master Python logic and data science.", "rfscVS0vtbw", "https://www.guru99.com/python-tutorials.html", "mindmap_python", "course_python", "Python")
        addCourse(coursesMap, "c2", "1", "JavaScript", "The language of the modern web.", "PkZNo7MFNFg", "https://archive.org/details/javascript-pdf", "mindmap_js", "course_javascript", "JavaScript")
        addCourse(coursesMap, "c3", "1", "Java Mastery", "Build robust enterprise applications.", "grEKMHGYyns", "https://www.iitk.ac.in/esc101/share/downloads/javanotes5.pdf", "mindmap_java", "course_java", "Java")
        addCourse(coursesMap, "c4", "1", "Kotlin for Android", "Official language for Android development.", "F9UC9DY-vIU", "https://kotlinlang.org/docs/kotlin-pdf.html", "mindmap_kotlin", "course_kotlin", "Kotlin")
        addCourse(coursesMap, "c5", "1", "C++ Systems", "High-performance systems programming.", "8jLOx1hD3_o", "https://cplusplus.com/files/tutorial.pdf", "mindmap_cpp", "course_cpp", "C++")
        addCourse(coursesMap, "c6", "1", "C Programming", "The foundation of all languages.", "KJgsSFOSQv0", "https://www.scribd.com/document/886161606/C-Notes", "mindmap_c", "course_c", "C")

        // CATEGORY 2: ANDROID
        addCourse(coursesMap, "a1", "2", "Android Basics", "Introduction to mobile architecture.", "fis26HvvDII", "https://www.scribd.com/document/849028843/Android", "mindmap_android_basics", "course_android_basics", "Android")
        addCourse(coursesMap, "a2", "2", "XML Layouts", "Designing traditional Android interfaces.", "PeCOKgAua7A", "https://www.scribd.com/document/823146722/xml", "mindmap_xml", "course_xml", "XML")
        addCourse(coursesMap, "a3", "2", "Jetpack Compose", "Modern declarative UI toolkit.", "6_wK_Ud8--0", "https://www.scribd.com/document/767880859/compose", "mindmap_compose", "course_compose", "Compose")
        addCourse(coursesMap, "a4", "2", "Room Database", "Robust local data persistence.", "bOd3wO0uFr8", "https://www.scribd.com/document/719225073/room", "mindmap_room", "course_room", "Room")
        addCourse(coursesMap, "a5", "2", "Firebase Cloud", "Real-time backend services for apps.", "4m-Wm3JsG84", "https://www.scribd.com/document/363784493/firebase", "mindmap_firebase", "course_firebase", "Firebase")

        // CATEGORY 3: WEB
        addCourse(coursesMap, "w1", "3", "HTML5 Mastery", "The structure of every website.", "pQN-pnXPaVg", "https://www.tutorialspoint.com/html/html_pdf_version.htm", "mindmap_html", "course_html", "HTML")
        addCourse(coursesMap, "w2", "3", "CSS3 Design", "Styling, layouts, and animations.", "OXGznpKZ_sA", "https://www.scribd.com/document/700809813/css", "mindmap_css", "course_css", "CSS")
        addCourse(coursesMap, "w3", "3", "React.js", "Building fast component-based UIs.", "bMknfKXIFA8", "https://react-pdf.org/", "mindmap_react", "course_react", "React")
        addCourse(coursesMap, "w4", "3", "PHP Server Side", "Dynamic server-side web logic.", "OK_JCtrrv-c", "https://gacbe.ac.in/images/php.pdf", "mindmap_php", "course_php", "PHP")
        addCourse(coursesMap, "w5", "3", "Web Security", "Protecting web applications from attacks.", "2p9mY0m0_0M", "https://owasp.org/www-pdf-archive/OWASP_Top_10_2017.pdf", "mindmap_web_sec", "ic_logo", "Security")

        // CATEGORY 4: AI
        addCourse(coursesMap, "ai1", "4", "AI Fundamentals", "Introduction to intelligent systems.", "5NgNicANyqM", "https://www.scribd.com/document/911781835/ai", "mindmap_ai_f", "course_ai_f", "AI")
        addCourse(coursesMap, "ai2", "4", "Machine Learning", "Predictive modeling and data patterns.", "i_LwzRVP7bg", "https://jnnc.ac.in/aiml/ml.pdf", "mindmap_ml", "course_ml", "ML")
        addCourse(coursesMap, "ai3", "4", "Deep Learning", "Neural vision and complex layers.", "VyWAvY2CF9c", "https://academicweb.nd.edu/dl.pdf", "mindmap_dl", "corse_dl", "DL")
        addCourse(coursesMap, "ai4", "4", "Neural Networks", "Building brain-inspired algorithms.", "Wo5dMEP_BbI", "https://introml.mit.edu/nn.pdf", "mindmap_neural", "course_neural", "Neural")
        addCourse(coursesMap, "ai5", "4", "Prompt Engineering", "Communicating effectively with LLMs.", "_ZvnD73m40o", "https://researchgate.net/prompt.pdf", "mindmap_prompt", "course_prompt", "Prompt")

        // CATEGORY 5: DATABASE
        addCourse(coursesMap, "db1", "5", "SQL Queries", "Standard relational database language.", "HXV3zeQKqGY", "https://riptutorial.com/ebook/sql", "mindmap_sql", "cpurse_sql", "SQL")
        addCourse(coursesMap, "db2", "5", "MySQL Admin", "Managing open-source database servers.", "7S_tz1z_5bA", "https://riptutorial.com/ebook/mysql", "mindmap_mysql", "cpurse_mysql", "MySQL")
        addCourse(coursesMap, "db3", "5", "SQLite Mobile", "Embedded local storage for mobile.", "byHcYRpMgI4", "https://archive.org/sqlite.pdf", "mindmap_sqllite", "cpurse_sqllite", "SQLite")
        addCourse(coursesMap, "db4", "5", "Cloud Firestore", "Scalable NoSQL database from Google.", "6aL0f0J2f6I", "https://firebase.google.com/docs/firestore", "mindmap_firestore", "course_firestore", "Firestore")
        addCourse(coursesMap, "db5", "5", "PostgreSQL Advanced", "Enterprise relational database management and scaling.", "X9h_9h_9h_9", "https://www.postgresql.org/docs/current/tutorial.html", "mindmap_sql", "course_database", "Database")

        // Atomic update
        val users = mapOf(
            "u1" to User("Admin", "User", "admin@mirixa.com", "u1", "admin", 4),
            "u2" to User("John", "Doe", "john@mirixa.com", "u2", "student", 0),
            "u3" to User("Jane", "Smith", "jane@mirixa.com", "u3", "student", 1),
            "u4" to User("Alex", "Developer", "alex@mirixa.com", "u4", "student", 2)
        )

        val updates = hashMapOf<String, Any>(
            "categories" to categories,
            "courses" to coursesMap,
            "users" to users,
            "db_version" to 3
        )
        database.updateChildren(updates)
            .addOnSuccessListener {
                Log.d("DatabaseSeeder", "Database seeded successfully!")
            }
            .addOnFailureListener { e ->
                Log.e("DatabaseSeeder", "Failed to seed database: ${e.message}")
            }
    }

    private fun addCourse(map: MutableMap<String, Course>, id: String, catId: String, title: String, desc: String, vId: String, pdf: String, mImg: String, iUrl: String, subject: String) {
        map[id] = Course(
            id = id,
            categoryId = catId,
            title = title,
            description = desc,
            imageUrl = iUrl,
            videoLecture = VideoLecture("https://www.youtube.com/watch?v=$vId", title, "15:00"),
            note = CourseNote(pdf, listOf(
                CourseTopic("Fundamentals", "Key building blocks of $subject.", "bulb"),
                CourseTopic("Implementation", "Applying $subject in real projects.", "code"),
                CourseTopic("Optimization", "Improving speed and efficiency.", "progress"),
                CourseTopic("Best Practices", "Industry-standard methodologies.", "star")
            )),
            mindmap = Mindmap(mImg, listOf(MindmapNode("n1", "Core", 150f, 150f), MindmapNode("n2", "Advanced", 350f, 350f))),
            quiz = getSubjectQuiz(subject)
        )
    }

    private fun getSubjectQuiz(subject: String): List<Question> {
        return when (subject) {
            "Python" -> listOf(
                Question("Who created Python?", listOf("James Gosling", "Guido van Rossum", "Dennis Ritchie", "Bjarne Stroustrup"), 1),
                Question("Which symbol is used for comments?", listOf("//", "#", "/*", "--"), 1),
                Question("Which keyword defines a function?", listOf("func", "function", "def", "define"), 2),
                Question("Correct extension for Python?", listOf(".py", ".python", ".txt", ".exe"), 0),
                Question("Correct way to start a list?", listOf("[]", "{}", "()", "<>"), 0),
                Question("Is Python case-sensitive?", listOf("Yes", "No", "Depends", "Only for variables"), 0),
                Question("Which is an immutable type?", listOf("List", "Set", "Tuple", "Dictionary"), 2),
                Question("Which function takes user input?", listOf("get()", "input()", "read()", "scan()"), 1),
                Question("What is the output of 2**3?", listOf("5", "6", "8", "9"), 2),
                Question("Which keyword starts a loop?", listOf("for", "each", "loop", "repeat"), 0)
            )
            "Kotlin" -> listOf(
                Question("Kotlin developer?", listOf("Google", "JetBrains", "Oracle", "IBM"), 1),
                Question("Read-only variable?", listOf("var", "val", "const", "let"), 1),
                Question("Null safety operator?", listOf("!!", "?.", "??", "::"), 1),
                Question("Function keyword?", listOf("func", "fun", "method", "def"), 1),
                Question("Main file extension?", listOf(".kt", ".kotlin", ".java", ".xml"), 0),
                Question("Primary constructor syntax?", listOf("init", "constructor", "Class()", "new"), 2),
                Question("Data class benefit?", listOf("Fast UI", "Reduced boilerplate", "Encrypted", "Offline only"), 1),
                Question("Default visibility?", listOf("private", "protected", "public", "internal"), 2),
                Question("Safe cast operator?", listOf("as?", "is", "cast", "check"), 0),
                Question("Elvis operator?", listOf("?:", "!!", "?.", "&&"), 0)
            )
            "Java" -> listOf(
                Question("Java developer?", listOf("Microsoft", "Sun/Oracle", "Google", "Apple"), 1),
                Question("JVM stands for?", listOf("Java Virtual Machine", "Visual Model", "Vertex Map", "Value Machine"), 0),
                Question("Object creation keyword?", listOf("create", "new", "init", "start"), 1),
                Question("Interface implementation keyword?", listOf("implements", "extends", "inherits", "using"), 0),
                Question("Static main arguments type?", listOf("String[]", "int", "void", "Object"), 0),
                Question("Final class benefit?", listOf("Fast loading", "Prevents inheritance", "No objects allowed", "Encrypted"), 1),
                Question("Exception handling block?", listOf("try-catch", "if-else", "for-loop", "switch"), 0),
                Question("Protected access means?", listOf("Public", "Package and subclass", "Private", "Global"), 1),
                Question("Package definition keyword?", listOf("import", "package", "namespace", "using"), 1),
                Question("JDK stands for?", listOf("Java Kit", "Development Kit", "Design Kit", "Domain Kit"), 1)
            )
            "Android" -> listOf(
                Question("Official IDE?", listOf("Eclipse", "Android Studio", "VS Code", "Netbeans"), 1),
                Question("UI layout format?", listOf("JSON", "XML", "HTML", "YAML"), 1),
                Question("Component for a screen?", listOf("Service", "Activity", "Broadcast", "Provider"), 1),
                Question("Manifest root tag?", listOf("<app>", "<manifest>", "<android>", "<activity>"), 1),
                Question("Gradle is primarily a?", listOf("Language", "Build Tool", "Database", "Server"), 1),
                Question("Minimum SDK defines?", listOf("Target level", "Compile level", "Lower limit support", "Max power"), 2),
                Question("Logcat is used for?", listOf("Design", "Testing", "Debugging", "Release"), 2),
                Question("ViewBinding reduces?", listOf("Security", "Boilerplate code", "Database size", "Networking"), 1),
                Question("Context provider?", listOf("Activity", "View", "Layout", "Drawable"), 0),
                Question("Fragment purpose?", listOf("Full screen only", "Reusable sub-activity", "Database link", "Network call"), 1)
            )
            "XML" -> listOf(
                Question("What does XML stand for?", listOf("Extra Markup", "Extensible Markup Language", "Extended Machine", "External Markup"), 1),
                Question("XML usage in Android?", listOf("Database creation", "UI design and data storage", "Kotlin compiling", "Server management"), 1),
                Question("XML file extension?", listOf(".xml", ".layout", ".ui", ".android"), 0),
                Question("Display text tag?", listOf("<TextView>", "<Text>", "<Label>", "<String>"), 0),
                Question("Width attribute?", listOf("android:height", "android:width", "android:layout_width", "android:size"), 2),
                Question("Fit content value?", listOf("match_parent", "wrap_content", "fill_parent", "content_size"), 1),
                Question("Assignment of ID?", listOf("android:name", "android:id", "android:key", "android:viewId"), 1),
                Question("Linear layout flow?", listOf("Vertical/Horizontal", "Layered", "Table", "Web only"), 0),
                Question("Text input View?", listOf("TextView", "ImageView", "EditText", "ViewGroup"), 2),
                Question("Root element?", listOf("ViewGroup/View", "MainActivity", "Manifest", "Gradle"), 0)
            )
            "Compose" -> listOf(
                Question("What is Jetpack Compose?", listOf("A database", "Native UI toolkit", "Language", "Testing tool"), 1),
                Question("Marking a function?", listOf("@Composable", "@Android", "@UI", "@Compose"), 0),
                Question("Primary language?", listOf("JavaScript", "Kotlin", "Java", "C++"), 1),
                Question("Text display function?", listOf("Text()", "Label()", "TextView()", "DisplayText()"), 0),
                Question("Vertical arrangement?", listOf("Row()", "Column()", "Box()", "Vertical()"), 1),
                Question("Horizontal arrangement?", listOf("Column()", "Box()", "Row()", "Horizontal()"), 2),
                Question("Clickable button?", listOf("Click()", "Button()", "Touchable()", "ActionButton()"), 1),
                Question("Preview annotation?", listOf("@Preview", "@Show", "@Design", "@View"), 0),
                Question("Efficient list?", listOf("LazyColumn()", "ScrollView()", "ListView()", "Recycler()"), 0),
                Question("Recomposition happens when?", listOf("App installs", "State changes", "Java compiles", "DB created"), 1)
            )
            "Room" -> listOf(
                Question("What is Room?", listOf("UI framework", "Database library", "Language", "Testing tool"), 1),
                Question("Underlying database?", listOf("MySQL", "Postgres", "SQLite", "Firebase"), 2),
                Question("Entity annotation?", listOf("@Database", "@Entity", "@Table", "@Model"), 1),
                Question("DAO annotation?", listOf("@Dao", "@Data", "@Repository", "@Query"), 0),
                Question("DAO means?", listOf("Data Access Object", "DB App Object", "Data Android Op", "DB Access Op"), 0),
                Question("SQL query annotation?", listOf("@SQL", "@Select", "@Query", "@DBQuery"), 2),
                Question("Primary key marker?", listOf("@Key", "@Primary", "@PrimaryKey", "@ID"), 2),
                Question("Database creator class?", listOf("RoomDatabase", "SQLiteDatabase", "DatabaseRoom", "RoomManager"), 0),
                Question("DAO main purpose?", listOf("UI design", "Data manipulation", "Create activities", "Manage images"), 1),
                Question("UI integration tool?", listOf("ViewModel", "Intent", "Activity", "Manifest"), 0)
            )
            "Firebase" -> listOf(
                Question("What is Firebase?", listOf("Language", "Web/Mobile platform", "OS", "DB only"), 1),
                Question("User auth service?", listOf("Firebase Auth", "Storage", "Hosting", "Analytics"), 0),
                Question("JSON tree database?", listOf("Firestore", "Realtime Database", "MySQL", "SQLite"), 1),
                Question("Document/Collection DB?", listOf("Storage", "Cloud Firestore", "Auth", "Hosting"), 1),
                Question("File storage service?", listOf("Firebase Storage", "Auth", "Firestore", "Functions"), 0),
                Question("Auth main purpose?", listOf("Store videos", "Manage sign-in", "UI design", "Kotlin compiling"), 1),
                Question("Config file?", listOf("firebase.xml", "google-services.json", "firebase.kt", "database.json"), 1),
                Question("Security Rules purpose?", listOf("Screen design", "Data access control", "Animations", "Compiling"), 1),
                Question("Usage analytics?", listOf("Firebase Analytics", "Storage", "Auth", "Database"), 0),
                Question("Sync-ready NoSQL?", listOf("Realtime Database", "Storage", "Auth", "Hosting"), 0)
            )
            "HTML" -> listOf(
                Question("HTML stands for?", listOf("Hyper Text Markup Language", "High Text Machine", "Hyperlink Text", "Home Tool"), 0),
                Question("Largest heading?", listOf("<h6>", "<heading>", "<h1>", "<head>"), 2),
                Question("Paragraph tag?", listOf("<para>", "<p>", "<text>", "<paragraph>"), 1),
                Question("Hyperlink tag?", listOf("<link>", "<a>", "<href>", "<url>"), 1),
                Question("Image source attribute?", listOf("href", "src", "link", "source"), 1),
                Question("Unordered list?", listOf("<ol>", "<list>", "<ul>", "<li>"), 2),
                Question("Image insert?", listOf("<image>", "<img>", "<picture>", "<src>"), 1),
                Question("Line break?", listOf("<break>", "<lb>", "<br>", "<newline>"), 2),
                Question("Visible content container?", listOf("<head>", "<body>", "<meta>", "<title>"), 1),
                Question("HTML5 declaration?", listOf("<!DOCTYPE html>", "<html5>", "<!HTML>", "<doctype>"), 0)
            )
            "CSS" -> listOf(
                Question("CSS stands for?", listOf("Computer Style", "Cascading Style Sheets", "Creative Style", "Colorful Style"), 1),
                Question("Text color property?", listOf("font-color", "text-color", "color", "foreground"), 2),
                Question("Background color?", listOf("bgcolor", "background-color", "background", "color-bg"), 1),
                Question("ID selector symbol?", listOf(".", "#", "@", "*"), 1),
                Question("Class selector symbol?", listOf("#", ".", "@", "$"), 1),
                Question("Font size property?", listOf("font-size", "text-size", "font-height", "size"), 0),
                Question("Inside space property?", listOf("margin", "padding", "spacing", "border"), 1),
                Question("Outside space property?", listOf("padding", "margin", "spacing", "outside"), 1),
                Question("Bold text property?", listOf("font-style", "font-weight", "text-bold", "font-bold"), 1),
                Question("Flexible layout?", listOf("block", "inline", "flex", "static"), 2)
            )
            "JavaScript" -> listOf(
                Question("JavaScript purpose?", listOf("Styling", "Interactivity", "DB creation", "Images"), 1),
                Question("Mutable variable?", listOf("var", "let", "const", "define"), 1),
                Question("Immutable variable?", listOf("let", "var", "const", "static"), 2),
                Question("Browser console log?", listOf("print()", "console.log()", "display()", "write()"), 1),
                Question("Single line comment?", listOf("//", "#", "<!--", "**"), 0),
                Question("Strict equality?", listOf("=", "==", "===", "!="), 2),
                Question("Function definition?", listOf("function", "def", "fun", "method"), 0),
                Question("Add to end of array?", listOf("add()", "push()", "append()", "insert()"), 1),
                Question("HTML interaction object?", listOf("window", "document", "html", "page"), 1),
                Question("Select by ID?", listOf("getElementById()", "selectId()", "getId()", "find()"), 0)
            )
            "React" -> listOf(
                Question("What is React?", listOf("Database", "UI Library", "Language", "OS"), 1),
                Question("Developer?", listOf("Google", "Facebook", "Microsoft", "Oracle"), 1),
                Question("React component?", listOf("Reusable UI block", "DB table", "CSS file", "Server"), 0),
                Question("Markup syntax?", listOf("SQL", "JSX", "XML", "PHP"), 1),
                Question("State hook?", listOf("useState", "useData", "useValue", "useComp"), 0),
                Question("Effect hook?", listOf("useState", "useEffect", "useSide", "useAction"), 1),
                Question("What are props?", listOf("Component data", "CSS styles", "DB records", "Tags"), 0),
                Question("Virtual DOM?", listOf("Browser", "UI representation", "Database", "Framework"), 1),
                Question("Start command?", listOf("npm run dev", "npm start", "react start", "run react"), 0),
                Question("Reusable?", listOf("No", "Once", "Yes", "CSS only"), 2)
            )
            "PHP" -> listOf(
                Question("PHP usage?", listOf("Server-side dev", "Images", "OS", "Hardware"), 0),
                Question("Full form?", listOf("Personal Page", "Hypertext Preprocessor", "Private Processor", "Programming Page"), 1),
                Question("PHP block markers?", listOf("<php>", "<?php ?>", "<PHP>", "{php}"), 1),
                Question("Variable prefix?", listOf("#", "@", "$", "%"), 2),
                Question("Output statement?", listOf("print", "echo", "display", "write"), 1),
                Question("Concatenation?", listOf("+", "&", ".", "::"), 2),
                Question("Array count?", listOf("count()", "size()", "length()", "total()"), 0),
                Question("Function keyword?", listOf("function", "def", "fun", "method"), 0),
                Question("POST data global?", listOf("\$_GET", "\$_POST", "\$_FORM", "\$_DATA"), 1),
                Question("Statement terminator?", listOf(":", ".", ";", ","), 2)
            )
            "AI" -> listOf(
                Question("AI stands for?", listOf("Automated Internet", "Artificial Intelligence", "Advanced Info", "Integration"), 1),
                Question("Main AI goal?", listOf("Store files", "Human-like tasks", "Create websites", "Speed"), 1),
                Question("AI example?", listOf("Calculator", "Voice assistant", "Keyboard", "USB"), 1),
                Question("Learn from data?", listOf("Machine Learning", "Web Design", "Networking", "Database"), 0),
                Question("NLP?", listOf("Learning Program", "Natural Language Proc.", "Logic Program", "Platform"), 1),
                Question("Specific task AI?", listOf("Narrow AI", "General AI", "Super AI", "Human AI"), 0),
                Question("Computer vision?", listOf("Hardware", "Visual understanding", "Files", "Code"), 1),
                Question("Teaching data?", listOf("Training data", "Deleted", "Password", "Hardware"), 0),
                Question("Recommendation tech?", listOf("AI/ML", "HTML", "CSS", "BIOS"), 0),
                Question("Generative AI goal?", listOf("Store data", "Content creation", "Numbers", "Manage"), 1)
            )
            "ML" -> listOf(
                Question("What is ML?", listOf("Learning patterns", "Database", "Language", "Hardware"), 0),
                Question("Labeled data type?", listOf("Unsupervised", "Supervised", "Reinforcement", "Random"), 1),
                Question("Unlabeled data type?", listOf("Supervised", "Unsupervised", "Reinforcement", "Manual"), 1),
                Question("Rewards method?", listOf("Supervised", "Unsupervised", "Reinforcement", "Static"), 2),
                Question("Model input var?", listOf("Feature", "Password", "Language", "Database"), 0),
                Question("Data collection?", listOf("Dataset", "Processor", "UI", "Cable"), 0),
                Question("Classification goal?", listOf("Predict categories", "Store data", "Compress", "Design"), 0),
                Question("Regression goal?", listOf("Numerical values", "Categories", "Images", "DBs"), 0),
                Question("Overfitting means?", listOf("Learning too close", "Deleted data", "Shutdown", "No data"), 0),
                Question("Accuracy measure?", listOf("Correct predictions", "Storage", "Speed", "Files"), 0)
            )
            "DL" -> listOf(
                Question("What is DL?", listOf("Neural networks ML", "Database", "Framework", "OS"), 0),
                Question("Deep network?", listOf("Multi-layer", "Many tables", "Large site", "Network"), 0),
                Question("One dataset pass?", listOf("Epoch", "Neuron", "Language", "Query"), 0),
                Question("Error measure?", listOf("Loss function", "Storage", "UI", "Wi-Fi"), 0),
                Question("Hardware accelerator?", listOf("GPU", "Keyboard", "Printer", "Router"), 0),
                Question("Weight update?", listOf("Backpropagation", "DB creation", "Emails", "Websites"), 0),
                Question("Image network?", listOf("CNN", "HTML", "SQL", "FTP"), 0),
                Question("Sequence network?", listOf("RNN", "CSS", "SQL", "XML"), 0),
                Question("Non-linearity tool?", listOf("Activation function", "Storage", "Files", "Devices"), 0),
                Question("Training goal?", listOf("Improve predictions", "Upgrade", "Delete files", "Redesign"), 0)
            )
            "Neural" -> listOf(
                Question("Network inspiration?", listOf("Human brain", "Database", "Browser", "Keyboard"), 0),
                Question("Processing unit?", listOf("Neuron", "Database", "Pixel", "Thread"), 0),
                Question("Receives data?", listOf("Input layer", "Output layer", "Hidden", "Final"), 0),
                Question("Final prediction?", listOf("Input", "Hidden", "Output layer", "Data"), 2),
                Question("Connection strength?", listOf("Weights", "Images", "Records", "Passwords"), 0),
                Question("Learnable offset?", listOf("Bias", "Error", "Password", "Hardware"), 0),
                Question("Neuron transform?", listOf("Activation", "Storage", "Database", "Internet"), 0),
                Question("Pass through flow?", listOf("Forward prop", "Deleting", "Updating", "Compressing"), 0),
                Question("Backward prop?", listOf("Update parameters", "Send server", "Interface", "Webpage"), 0),
                Question("Network task?", listOf("Classification", "Storage", "Processing", "Backup"), 0)
            )
            "Prompt" -> listOf(
                Question("What is Prompt Eng?", listOf("Designing instructions", "CPU prog", "DBs", "Hardware"), 0),
                Question("What is a prompt?", listOf("Instruction/Input", "Database", "Language", "Component"), 0),
                Question("Specific prompt goal?", listOf("Better relevance", "Larger file", "Slower AI", "Delete data"), 0),
                Question("Good prompt part?", listOf("Task and context", "Random", "No instructions", "Unrelated"), 0),
                Question("Zero-shot?", listOf("No examples", "One example", "Many", "Training"), 0),
                Question("Few-shot?", listOf("Small examples", "No input", "Hardware", "Deleting"), 0),
                Question("Context goal?", listOf("Situation understanding", "Increase RAM", "Change OS", "DB"), 0),
                Question("Role prompting?", listOf("Follow perspective", "Account", "Java code", "Table"), 0),
                Question("Structure instruction?", listOf("Output format", "Deleting", "Hardware", "Wi-Fi"), 0),
                Question("Better prompt?", listOf("Explain ML simply", "ML.", "Tell.", "Do."), 0)
            )
            "SQL" -> listOf(
                Question("SQL full form?", listOf("Structured Query Language", "Simple", "System", "Question"), 0),
                Question("Retrieve command?", listOf("GET", "SELECT", "FETCH", "READ"), 1),
                Question("Add command?", listOf("ADD", "INSERT", "CREATE", "PUT"), 1),
                Question("Modify command?", listOf("CHANGE", "UPDATE", "MODIFY", "EDIT"), 1),
                Question("Remove command?", listOf("REMOVE", "DELETE", "DROP", "CLEAR"), 1),
                Question("Filter clause?", listOf("FILTER", "WHERE", "SELECT", "HAVING"), 1),
                Question("Unique record key?", listOf("Foreign Key", "Primary Key", "Candidate", "Secondary"), 1),
                Question("Create table?", listOf("MAKE", "CREATE TABLE", "NEW", "ADD"), 1),
                Question("Sort results?", listOf("SORT", "ORDER BY", "GROUP", "ARRANGE"), 1),
                Question("Row count?", listOf("SUM()", "TOTAL()", "COUNT()", "NUMBER()"), 2)
            )
            "MySQL" -> listOf(
                Question("What is MySQL?", listOf("Language", "Relational DBMS", "OS", "Browser"), 1),
                Question("Interaction language?", listOf("SQL", "HTML", "CSS", "Kotlin"), 0),
                Question("DB selection?", listOf("USE", "SELECT DATABASE", "OPEN", "START"), 0),
                Question("DB creation?", listOf("NEW", "CREATE DATABASE", "MAKE", "ADD"), 1),
                Question("Show DBs?", listOf("SHOW DATABASES", "LIST", "GET", "VIEW"), 0),
                Question("Text data type?", listOf("INT", "VARCHAR", "FLOAT", "DATE"), 1),
                Question("Add record?", listOf("INSERT INTO", "ADD", "PUT", "CREATE"), 0),
                Question("Drop table?", listOf("DELETE", "REMOVE", "DROP TABLE", "CLEAR"), 2),
                Question("GUI tool?", listOf("Workbench", "Browser", "Studio", "Editor"), 0),
                Question("Model type?", listOf("Relational", "Hierarchical", "Graph", "Document"), 0)
            )
            "SQLite" -> listOf(
                Question("What is SQLite?", listOf("Serverless engine", "Language", "Cloud", "Framework"), 0),
                Question("Server required?", listOf("Yes", "No", "Android only", "Windows only"), 1),
                Question("Storage location?", listOf("DB file", "RAM only", "Cloud only", "HTML"), 0),
                Question("Query language?", listOf("SQL", "XML", "JSON", "Kotlin"), 0),
                Question("Common use?", listOf("Embedded apps", "Cloud servers", "Websites", "OS"), 0),
                Question("Android build?", listOf("Room", "Firebase", "Firestore", "Retrofit"), 0),
                Question("Retrieve?", listOf("GET", "SELECT", "READ", "FETCH"), 1),
                Question("Text type?", listOf("TEXT", "STRING", "VARCHAR", "CHAR"), 0),
                Question("Major advantage?", listOf("Lightweight", "Large server", "Cloud only", "Internet"), 0),
                Question("File extension?", listOf(".db", ".sql", ".sqlite", ".mysql"), 0)
            )
            "Firestore" -> listOf(
                Question("What is Firestore?", listOf("Cloud NoSQL", "Language", "UI Library", "Local DB"), 0),
                Question("Organization?", listOf("Tables", "Collections/Documents", "Files", "Arrays"), 1),
                Question("Document goal?", listOf("Record with fields", "Server", "Language", "Screen"), 0),
                Question("Collection goal?", listOf("Group of documents", "Single field", "Password", "Activity"), 0),
                Question("Relational?", listOf("Yes", "No", "Android only", "SQL only"), 1),
                Question("Auth source?", listOf("Firebase Auth", "Firestore", "Storage", "Hosting"), 0),
                Question("Client sync?", listOf("Yes", "No", "Offline only", "Desktop"), 0),
                Question("Security Rules?", listOf("Data access", "Colors", "Icon", "Syntax"), 0),
                Question("Type?", listOf("NoSQL document", "Relational", "File sys", "Graph"), 0),
                Question("Alternative?", listOf("Realtime Database", "SQLite", "Room", "MySQL"), 0)
            )
            else -> listOf(
                Question("$subject: Goal?", listOf("Efficiency", "Storage", "UI", "All"), 3),
                Question("$subject: Concept?", listOf("Syntax", "Logic", "Design", "Hardware"), 1),
                Question("$subject: Tool?", listOf("IDE", "Browser", "Compiler", "Debugger"), 0),
                Question("$subject: Practice?", listOf("Clean code", "Fast", "No comments", "Short"), 0),
                Question("$subject: Use case?", listOf("Apps", "Websites", "Security", "All"), 3),
                Question("$subject: Extension?", listOf(".ext", ".data", ".src", "Subject"), 3),
                Question("$subject: Storage?", listOf("Variables", "Arrays", "Database", "All"), 3),
                Question("$subject: Logic?", listOf("If-else", "Loops", "Functions", "All"), 3),
                Question("$subject: Security?", listOf("Validation", "Encryption", "Privacy", "All"), 3),
                Question("$subject: Path?", listOf("Practice", "Reading", "Video", "All"), 3)
            )
        }
    }
}
