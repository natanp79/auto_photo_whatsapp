import 'package:photo_manager/photo_manager.dart';

class PhotoService {
  static Future<List<AssetEntity>> getLatestTenPhotos() async {
    final permission = await PhotoManager.requestPermissionExtend();

    if (!permission.hasAccess) {
      return [];
    }

    final albums = await PhotoManager.getAssetPathList(
      type: RequestType.image,
      onlyAll: true,
    );

    if (albums.isEmpty) {
      return [];
    }

    final photos = await albums.first.getAssetListRange(
      start: 0,
      end: 10,
    );

    return photos;
  }
}
