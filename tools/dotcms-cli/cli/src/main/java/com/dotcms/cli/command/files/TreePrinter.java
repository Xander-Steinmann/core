package com.dotcms.cli.command.files;

import com.dotcms.api.traversal.TreeNode;
import com.dotcms.cli.common.FilesUtils;
import com.dotcms.common.AssetsUtils;
import com.dotcms.model.asset.AssetView;
import com.dotcms.model.asset.FolderView;
import com.dotcms.model.language.Language;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * The {@code TreePrinter} class provides a utility for printing a tree structure of
 * {@link AssetView}s and {@link FolderView}s. The tree can be filtered by language and asset status
 * (live or working).
 *
 * <p>This class uses the singleton pattern, and can be accessed by calling {@link #getInstance()}.
 */
public class TreePrinter {

    final String statusRegularFormat = "@|bold %s|@";
    final String languageRegularFormat = "@|bold \uD83C\uDF10 %s|@";
    final String folderRegularFormat = "\uD83D\uDCC2 %s";
    final String folderDeleteFormat = "@|bold,red \uD83D\uDCC2 %s \u2716|@";
    final String folderPushFormat = "@|bold,green \uD83D\uDCC2 %s \u2795|@";
    final String siteRegularFormat = "@|bold %s|@";
    final String sitePushFormat = "@|bold,green %s \u2795|@";
    final String assetRegularFormat = "%s";
    final String assetPushNewFormat = "@|bold,green %s \u2795|@";
    final String assetPushModifiedFormat = "@|bold,cyan %s \u270E|@";
    final String assetDeleteFormat = "@|bold,red %s \u2716|@";

    /**
     * The {@code TreePrinterHolder} class is used to implement the singleton pattern for the
     * {@link TreePrinter} class.
     */
    private static class TreePrinterHolder {
        private static final TreePrinter instance = new TreePrinter();
    }

    /**
     * Returns the singleton instance of the {@code TreePrinter} class.
     *
     * @return the singleton instance of the {@code TreePrinter} class.
     */
    public static TreePrinter getInstance() {
        return TreePrinterHolder.instance;
    }

    private TreePrinter() {
    }

    /**
     * Prints {@link TreeNode} structures to the provided {@link StringBuilder}.
     * The tree structure is formatted focusing on the push data contained on each asset and folder.
     *
     * @param sb               the {@link StringBuilder} to append the tree structure to.
     * @param treesNodeData    A list of pairs, where each pair represents a folder's local path structure
     *                         and its corresponding tree node
     * @param showEmptyFolders A boolean indicating whether to include empty folders in the tree. If
     *                         set to true, all folders will be included. If set to false, only
     *                         folders containing assets or having children with assets will be
     *                         included.
     */
    public void formatForPush(StringBuilder sb,
                              List<Pair<AssetsUtils.LocalPathStructure, TreeNode>> treesNodeData,
                              final boolean showEmptyFolders) {

        // Let's try to print these tree with some order
        treesNodeData.sort((o1, o2) -> {
            var left = o1.getLeft();
            var right = o2.getLeft();
            return left.filePath().compareTo(right.filePath());
        });

        var count = 0;

        for (var treeNodeData : treesNodeData) {

            sb.append(count++ == 0 ? "\r" : "\n").
                    append(String.format(" Site: [%s] - Status [%s] - Language [%s] --- Folder [%s]\n",
                            treeNodeData.getLeft().site(),
                            treeNodeData.getLeft().status(),
                            treeNodeData.getLeft().language(),
                            treeNodeData.getLeft().filePath()));

            var localPathStructure = treeNodeData.getLeft();
            var treeNode = treeNodeData.getRight();

            var treeNodePushInfo = treeNode.collectTreeNodePushInfo();

            if (treeNodePushInfo.hasChanges()) {

                var assetsToPushCount = treeNodePushInfo.assetsToPushCount();
                if (assetsToPushCount > 0) {
                    sb.append(String.format(" Push Data: " +
                                    "@|bold,green [%s]|@ Assets to push: (%s New - %s Modified) " +
                                    "- @|bold,green [%s]|@ Assets to delete " +
                                    "- @|bold,green [%s]|@ Folders to push " +
                                    "- @|bold,green [%s]|@ Folders to delete\n\n",
                            treeNodePushInfo.assetsToPushCount(),
                            treeNodePushInfo.assetsNewCount(),
                            treeNodePushInfo.assetsModifiedCount(),
                            treeNodePushInfo.assetsToDeleteCount(),
                            treeNodePushInfo.foldersToPushCount(),
                            treeNodePushInfo.foldersToDeleteCount()));
                } else {
                    sb.append(String.format(" Push Data: " +
                                    "@|bold,green [%s]|@ Assets to push " +
                                    "- @|bold,green [%s]|@ Assets to delete " +
                                    "- @|bold,green [%s]|@ Folders to push " +
                                    "- @|bold,green [%s]|@ Folders to delete\n\n",
                            treeNodePushInfo.assetsToPushCount(),
                            treeNodePushInfo.assetsToDeleteCount(),
                            treeNodePushInfo.foldersToPushCount(),
                            treeNodePushInfo.foldersToDeleteCount()));
                }

                formatByStatus(
                        sb,
                        AssetsUtils.StatusToBoolean(localPathStructure.status()),
                        List.of(localPathStructure.language()),
                        treeNode,
                        showEmptyFolders,
                        true);
            } else {
                sb.append(" No changes to push\n\n");
            }

        }
    }

    /**
     * Prints a filtered tree structure of the specified {@link TreeNode} to the provided
     * {@link StringBuilder}. The tree structure is filtered by language and asset status (live or
     * working).
     *
     * @param sb               the {@link StringBuilder} to append the tree structure to.
     * @param rootNode         the root node of the tree structure.
     * @param showEmptyFolders A boolean indicating whether to include empty folders in the tree. If
     *                         set to true, all folders will be included. If set to false, only
     *                         folders containing assets or having children with assets will be
     *                         included.
     */
    public void filteredFormat(StringBuilder sb,
            TreeNode rootNode,
            final boolean showEmptyFolders,
            final List<Language> languages) {

        // Collect the list of unique statuses and languages
        final var treeNodeInfo = rootNode.collectUniqueStatusesAndLanguages(showEmptyFolders);
        final var uniqueLiveLanguages = treeNodeInfo.liveLanguages();
        final var uniqueWorkingLanguages = treeNodeInfo.workingLanguages();

        if (uniqueLiveLanguages.isEmpty() && uniqueWorkingLanguages.isEmpty()) {
            FilesUtils.FallbackDefaultLanguage(languages, uniqueLiveLanguages);
        }

        // Sort the sets and convert them into lists
        List<String> sortedLiveLanguages = new ArrayList<>(uniqueLiveLanguages);
        Collections.sort(sortedLiveLanguages);

        List<String> sortedWorkingLanguages = new ArrayList<>(uniqueWorkingLanguages);
        Collections.sort(sortedWorkingLanguages);

        // Live tree
        formatByStatus(sb, true, sortedLiveLanguages, rootNode, showEmptyFolders, false);
        // Working tree
        formatByStatus(sb, false, sortedWorkingLanguages, rootNode, showEmptyFolders, false);
    }

    /**
     * Formats a StringBuilder object with the assets and their status in the given TreeNode
     * recursively using a short format for each asset in the sortedLanguages list, separated by
     * status.
     *
     * @param sb               The StringBuilder object to format.
     * @param isLive           A boolean indicating whether the status to format is Live or Working.
     * @param sortedLanguages  A List of Strings containing the languages to include in the formatted
     *                         StringBuilder.
     * @param rootNode         The root TreeNode to start the formatting from.
     * @param showEmptyFolders A boolean indicating whether to include empty folders in the tree. If
     *                         set to true, all folders will be included. If set to false, only folders
     *                         containing assets or having children with assets will be included.
     * @param forPushChanges   A boolean indicating whether the formatting is for push changes or not.
     */
    private void formatByStatus(StringBuilder sb, boolean isLive, List<String> sortedLanguages,
                                TreeNode rootNode, final boolean showEmptyFolders, final boolean forPushChanges) {

        if (sortedLanguages.isEmpty()) {
            return;
        }

        // Calculate the parent path for this first node
        String parentPath = calculateRootParentPath(rootNode);
        Path initialPath;
        try {
            initialPath = Paths.get(parentPath);
        } catch (InvalidPathException e) {
            var error = String.format("Invalid folder path [%s] provided", parentPath);
            throw new IllegalArgumentException(error, e);
        }

        var status = AssetsUtils.StatusToString(isLive);
        sb.append("\r ").append(String.format(statusRegularFormat, status)).append('\n');

        Iterator<String> langIterator = sortedLanguages.iterator();
        while (langIterator.hasNext()) {

            String lang = langIterator.next();
            TreeNode filteredRoot = rootNode.cloneAndFilterAssets(isLive, lang, showEmptyFolders, forPushChanges);

            // Print the filtered tree using the format method starting from the filtered root itself
            boolean isLastLang = !langIterator.hasNext();
            sb.append("     ").
                    append((isLastLang ? "└── " : "├── ")).
                    append(String.format(languageRegularFormat, lang)).append('\n');

            var siteFormat = siteRegularFormat;
            if (rootNode.folder().markForPush().isPresent()) {
                if (rootNode.folder().markForPush().get()) {
                    siteFormat = sitePushFormat;
                }
            }

            // Add the domain and parent folder
            sb.append("     ").
                    append((isLastLang ? "    " : "│   ")).
                    append("└── ").
                    append(String.format(siteFormat, rootNode.folder().host())).
                    append('\n');

            if (parentPath.isEmpty() || parentPath.equals("/")) {
                format(sb,
                        "     " + (isLastLang ? "    " : "│   ") + "    ",
                        filteredRoot,
                        "     " + (isLastLang ? "    " : "│   ") + "    ",
                        true, true);
            } else {

                // If the initial path is not the root we need to try to print the folder structure it has
                var parentFolderIdent = isLastLang ? "    " : "";
                for (var i = 0; i < initialPath.getNameCount(); i++) {
                    sb.append("     ").
                            append((isLastLang ? "    " : "│   ")).
                            append(parentFolderIdent).
                            append((isLastLang ? "" : "    ")).
                            append("└── ").
                            append(String.format("@|bold \uD83D\uDCC2 %s|@", initialPath.getName(i))).
                            append('\n');
                    if (i + 1 < initialPath.getNameCount()) {
                        parentFolderIdent += "    ";
                    }
                }

                format(sb,
                        parentFolderIdent + "     " + (isLastLang ? "" : "│   ") + "        ",
                        filteredRoot,
                        parentFolderIdent + "     " + (isLastLang ? "" : "│   ") + "        ",
                        true, true);
            }
        }
    }

    /**
     * Creates a short representation of the given node and its children as a string, with options
     * for including or excluding asset details and controlling the indentation and prefix for each
     * line.
     *
     * @param sb            The `StringBuilder` to append the string representation to.
     * @param prefix        The string prefix to use for each line in the representation, before any
     *                      symbols.
     * @param node          The `TreeNode` to create a string representation of.
     * @param indent        The string to use for each level of indentation in the representation.
     * @param isLastSibling Whether the `TreeNode` is the last sibling in the current level of the
     *                      representation.
     * @param includeAssets Whether to include asset details in the representation.
     */
    private void format(StringBuilder sb, String prefix, final TreeNode node,
            final String indent, boolean isLastSibling, boolean includeAssets) {

        var folderFormat = folderRegularFormat;
        if (node.folder().markForDelete().isPresent()) {
            if (node.folder().markForDelete().get()) {
                folderFormat = folderDeleteFormat;
            }
        } else if (node.folder().markForPush().isPresent()) {
            if (node.folder().markForPush().get()) {
                folderFormat = folderPushFormat;
            }
        }

        String filePrefix;
        String nextIndent;
        if (!node.folder().name().equals("/")) {
            sb.append(prefix).
                    append(isLastSibling ? "└── " : "├── ").
                    append(String.format(folderFormat, node.folder().name())).
                    append('\n');

            filePrefix = indent + (isLastSibling ? "    " : "│   ");
            nextIndent = indent + (isLastSibling ? "    " : "│   ");
        } else {
            filePrefix = indent + (isLastSibling ? "" : "│   ");
            nextIndent = indent + (isLastSibling ? "" : "│   ");
        }

        if (includeAssets) {
            // Adds the names of the node's files to the string representation.
            int assetCount = node.assets().size();
            for (int i = 0; i < assetCount; i++) {

                // Calculate the asset format to use
                AssetView asset = node.assets().get(i);
                var assetFormat = assetRegularFormat;

                if (asset.markForDelete().isPresent()) {
                    if (asset.markForDelete().get()) {
                        assetFormat = assetDeleteFormat;
                    }
                }

                if (asset.markForPush().isPresent()) {
                    if (asset.markForPush().get()) {

                        assetFormat = assetPushModifiedFormat;

                        if (asset.pushTypeNew().isPresent()) {
                            if (asset.pushTypeNew().get()) {
                                assetFormat = assetPushNewFormat;
                            }
                        }
                    }
                }

                boolean lastAsset = i == assetCount - 1 && node.children().isEmpty();

                sb.append(filePrefix).
                        append(lastAsset ? "└── " : "├── ").
                        append(String.format(assetFormat, asset.name())).
                        append('\n');
            }
        }

        // Recursively creates string representations for the node's children.
        int childCount = node.children().size();
        for (int i = 0; i < childCount; i++) {
            TreeNode child = node.children().get(i);
            boolean lastSibling = i == childCount - 1;
            format(sb, filePrefix, child, nextIndent, lastSibling, includeAssets);
        }
    }

    /**
     * Calculates the parent path of the given root node.
     *
     * @param rootNode The root node to calculate the parent path from.
     * @return A String containing the root parent path.
     */
    private String calculateRootParentPath(TreeNode rootNode) {

        // Calculating the root folder path
        var folderPath = rootNode.folder().path();
        var folderName = rootNode.folder().name();

        // Determine if the folder path and folder name are empty or null
        var emptyFolderPath = folderPath == null
                || folderPath.isEmpty()
                || folderPath.equals("/");

        var emptyFolderName = folderName == null
                || folderName.isEmpty()
                || folderName.equals("/");

        // Remove firsts and last slash from folder path
        if (!emptyFolderPath) {
            folderPath = folderPath.
                    replaceAll("^/", "").
                    replaceAll("/$", "");
        }

        if (!emptyFolderName) {
            if (folderPath.endsWith(folderName)) {

                int folderIndex = folderPath.lastIndexOf(folderName);
                folderPath = folderPath.substring(0, folderIndex);

                folderPath = folderPath.
                        replaceAll("^/", "").
                        replaceAll("/$", "");
            }
        }

        return folderPath;
    }

}
