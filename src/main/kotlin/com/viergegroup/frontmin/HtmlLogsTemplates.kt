package com.viergegroup.frontmin

object HtmlLogsTemplates {
    private const val styles = """
        .container {
            margin-top: 5rem;
        }
        .card {
            border: 1px solid #e0e0e0;
            border-radius: 0.5rem;
            margin-bottom: 1rem;
            box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
        }
        .card-body {
            padding: 1rem;
        }
        .card-title {
            margin-bottom: 0.75rem;
            font-size: 1.25rem;
            font-weight: 500;
            color: #007bff;
            text-decoration: none;
        }
        .card-title a {
            color: #007bff;
            text-decoration: none;
        }
        .card-title a:hover {
            text-decoration: underline;
        }
        .card-text {
            margin-bottom: 0.5rem;
        }
        .text-muted {
            color: #6c757d;
        }
    """

    fun pageTemplate(logListHtml: String) = """
        <html>
        <head>
            <style>
                $styles
            </style>
        </head>
        <body>
            <div class="container">
                $logListHtml
            </div>
        </body>
        </html>
    """

    fun directoryCard(logFileName: String, encodedPath: String, lastModifiedTime: String) = """
        <div class="card mb-3">
            <div class="card-body">
                <h5 class="card-title"><a href="/logs/view?subpath=$encodedPath">$logFileName/</a></h5>
                <p class="card-text">Directory</p>
                <p class="card-text"><small class="text-muted">Last modified: $lastModifiedTime</small></p>
            </div>
        </div>
    """

    fun fileCard(logFileName: String, encodedPath: String, lastModifiedTime: String) = """
        <div class="card mb-3">
            <div class="card-body">
                <h5 class="card-title"><a href="/logs/view/file?path=$encodedPath">$logFileName</a></h5>
                <p class="card-text">File</p>
                <p class="card-text"><small class="text-muted">Last modified: $lastModifiedTime</small></p>
            </div>
        </div>
    """
}