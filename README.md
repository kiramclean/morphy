# Morphy

A simple, minimalist, general purpose static website generator written in Clojure.

[![Morphy static site generator circleci status](https://circleci.com/gh/kiramclean/morphy.svg?style=shield)](https://app.circleci.com/pipelines/github/kiramclean/morphy)
[![Morphy static site generator code coverage status](https://codecov.io/gh/kiramclean/morphy/branch/main/graph/badge.svg?token=SMXPW0BF1Q)](https://codecov.io/gh/kiramclean/morphy)

🚨**WARNING**🚨

This is uber alpha software; very much still a work in progress. It works for my purposes but is very likely going to change dramatically before I suggest anyone else use it. Consider this an unstable project for now. 

At the moment it only runs from a Clojure REPL and doesn't do much. I'm planning on making a CLI and all kinds of other things, but this is just a side project which I work on very sporadically.

## Overview

Morphy is a static website generator written in Clojure. It was born of my frustration trying to configure other static website generators. I prefer writing software over configuring software, so I made morphy for myself so I could have a blog. Right now all it does is convert Markdown files to HTML, and insert that content into templates, to finally generate a whole website.

I already know HTML and CSS, and I believe that is all one should have to know to build a simple static website, because that's all websites are made of. Turns out you need to learn a little bit about templating as well to make a website in any sort of practical way, but my goal is to allow the simplest possible structure for the source files of static websites generated by morphy. I found the existing static site generators incredibly frustrating to configure and completely failed to use some of them to make my really basic website, so I gave up and made my own.

## Goals

My ultimate goal with Morphy is to make it as simple as possible to generate a valid website from source files. These are some more specific goals of the project that reflect that:

- Support templating files, so content files can be minimalist and never repeat boilerplate 
- Support arbitrary source file structure -- no imposed directory structure for content
- Make site content available in templates in an intuitive way that mirrors the directory structure on the filesystem
- Have opinionated defaults to minimize the amount of metadata users have to specify. For simple websites there should be no configuration at all.
- Avoid specifying any config for site generation in separate config files as much as possible.
  - Favour inferring rules for site generation from file names, source directory structure, file extensions, etc. and when necessary favour adding metadata to individual pages in context, not in a separate location.
- Provide a simple UI for minimally technical people that can be installed as a stand-alone utility, not requiring a whole Clojure development environment.

## Non-Goals

- Comprehensive tooling for all kinds of extra file types (i.e. image processing, scss/sass, JavaScript dialects or frameworks)
- Drag and drop layouts or WSIWYG content editing.
  - CSS is [powerful enough](http://www.csszengarden.com) to create any kind of layout. I have no ambitions of building a Wix-style editor for rearranging layouts without writing CSS.
  - Whilst I don't think anyone should have to learn a whole new language or JavaScript framework to build a website, I think it's reasonable to work in HTML and CSS.
- Ultimate flexibility. There are well established patterns and best practices for building websites, which I want to encourage people to follow by making those the simplest way to use morphy.

## Concept

Right now Morphy supports content written in Markdown (with front matter) and templates written in Mustache. Think of Morphy as a pipeline and your content as data that flows through it. Markdown and Mustache files go in, complete HTML documents come out. This is what [the pipeline](https://github.com/kiramclean/morphy/blob/main/src/morphy/core.clj#L39-L43) does at the moment:

Starting with source files

-> load each page and parse the front matter, extracting a title, description, and slug (which are inferred from the content if none are specified in the front matter)

-> process the content of each page from Markdown to HTML

-> render each page that itself uses site data

-> insert each page into a layout

-> write these pages to the filesystem again as the final website

### File Processing

**Which source files get processed into a web page?**

- Any files _not_ in a directory called `assets` get processed by the pipeline.
- Any files that _are in a directory called `assets`_ get ignored and pass through unchanged, copied to a directory in the built website that mirrors their location in the source directory
- Any files that _are not named `index.html` or `index.md`_ get expanded into a directory of their own with an `index.html` file inside. E.g. a file called `posts/my-first-blog-post.md` will be `posts/my-first-blog-post/index.html` in the built website. This is purely a vanity thing, so urls in the final website don't all need to have `.html` appended to them to work. Note this means you should have a `rel="canonical"` link in the website head for each page.
- Directory structure is arbitrary and preserved in the built website. Slugs are assumed from the location of files in the source directory.
  - slugs are also over-writable in front-matter

**How Morphy decides which transformations are applied to which files**

- As mentioned above any file in any directory called `assets` is not processed and gets copied as-is to a mirrored directory in the output location
- Otherwise it's based on file extensions
  - Any file with the `.md` extension gets its content processed from Markdown to HTML
  - Any file with the `.mustache` extension gets processed by the template renderer 
- All files get inserted into the layout as the final step
  - This is possible to override by specifying `site/no-layout: true` in the front-matter for a given page. This is useful for e.g. a 404 page or RSS feed template that you do not want inserted into the main default layout.
  
**Naming and placement conventions**

Morphy looks in specific places for layout files.
- Morphy looks for a file specifically called `_layout.mustache` for the layout (the "wrapper" for each page's content) 
  - If none is found it just outputs the page content on its own (assuming no wrapper)
- Morphy will use the closest layout found for each page, so you can override layouts in a given directory. This is what I mean:
  ```
  ├── source
  │   ├── _layout.mustache <----- Think of this one as "root layout"
  │   ├── root-page.md <--------- This root page will be wrapped with its sibling
  │   │                           `_layout.mustache` ("root layout")
  │   ├── nested
  │   │   ├── _layout.mustache <--- Think of this one as "nested layout"
  │   │   └── nested-page.md <----- This nested page will be wrapped with the `_layout.mustache`
                                    that's _in the same directory_, NOT the one that's in the 
                                    root directory ("nested layout")
  ```
- You can specify a different layout for a given page by adding a `layout: layout-name` key to the front matter. Morphy will look for these named layouts in a root directory called `_layouts`.
- Mustache supports template partials (fragments of templates you can share to minimize duplication). Morphy assumes these partials are in a directory called `_partials`, and they are available by name in every page.
  - The same overriding mechanism the layouts use applies to partials as well. I.e. you can have a `_partials` directory nested inside any other directory and morphy will use the _closest partial with a given name_. You can see an example of how I use this to show a different header on different pages on my blog [here](https://github.com/kiramclean/blog/tree/main/site).

## Usage

I'm postponing writing proper documentation until this project is more stable because I want to minimize the number of times I have to rewrite documentation. I'm still experimenting with the best ways to accomplish the goals of the project so things are still quite unstable. I believe in working in the open, though, so that's why this work in progress is here on Github.

I do currently use this to build [my personal website](https://github.com/kiramclean/blog), so in theory so could you. This is how I currently do it:

- load `morphy.core` and switch to that namespace in a clojure REPL
- define these variables in the REPL:
  ```clojure
  (def input-dir "/Users/kira/code/projects/blog/site")
  (def output-dir "/Users/kira/code/projects/blog/dist")
  (def context {:input-dir input-dir
                :output-dir output-dir
                :root-url "https://kiramclean.com"
                :groups/sort-priority ["Recent"]})
  ```
- run `(generate-site context)`
- to see the website before deploying it, I switch into the `dist` directory and serve its contents with this alias I have defined in my shell profile:
  ```bash
  servedir='ruby -run -e httpd . -p 8000'
  ```

I'm more focused on settling the core of the generator now than building an adequate UX, but I plan on wrapping this in a stand-alone CLI with a built-in development server.

## Development

I try to keep my plans for this project organized in [this GitHub project](https://github.com/users/kiramclean/projects/2). It's not always exactly up to date, but should give a pretty good idea of where things are at.

The issues are intermingled with issues from my personal website, because both of these things compete for my free time, so I just keep one list.

### Tests

To run the project's tests:

    clj -M:test

To run the project's tests and report code coverage:

    clj -M:coverage

## License

Copyright © 2020 Kira McLean

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
