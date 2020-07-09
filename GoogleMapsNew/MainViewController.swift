//
//  MainViewController.swift
//  GoogleMapsNew
//
//  Created by Mohamed Salman on 2020/07/06.
//  Copyright © 2020 Mohamed Salman. All rights reserved.
//

import UIKit
import GoogleMaps

class MainViewController: UIViewController {
    
    let key = APIKeys.google_Directions_Key
    var waypoint_order_sorted : [Int] = []
    
    //array architecture[[longitude, latitude, Title, Description, IsDeliveryItem, Text to Display on Icon, IsToVisible]]
    let locations = [[53.5144554,-113.5907162, "DHL Office", "Alberta, Canada", true, "#0", true],[53.0962446,-111.7713151, "Viking", "Alberta, Canada", false, "#1111111", true], [53.5516051,-113.1515938, "Adrossan", "Alberta, Canada", true, "#2543432132", true],[53.007022,-112.8630558, "Camrosa", "Alberta, Canada", false, "#1242424522", true], [53.5887518,-112.3512429, "Mundare", "Alberta, Canada", true, "#ABCD7383", true],[52.5849501,-112.9769295, "Bashaw", "Alberta, Canada", false, "#43221", true], [52.5832894,-112.0769292, "Forestburg", "Alberta, Canada", true, "#ASJWI-SHS", true]]
    let path = GMSMutablePath()
    var sorted_location : [[Any]] = []
    override func viewDidLoad() {
        super.viewDidLoad()
        generateRoutes(locations: locations)
        
    }
    func generateMap()  {
        
        let camera = GMSCameraPosition.camera(withLatitude: locations[0][0] as! Double, longitude: locations[0][1] as! Double, zoom: 8)
        let mapView = GMSMapView.map(withFrame: self.view.frame, camera: camera)
        self.view.addSubview(mapView)
        addtoMaker(lati: locations[0][0] as! Double, longti: locations[0][1] as! Double, labelText: "start", title: "Start Point", description: "Starting From", mapView: mapView, isDelivery: false)
        for (index, info) in sorted_location.enumerated() {
            if let lati = info[0] as? Double {
                if let longti = info[1] as? Double {
                    if(info[6] as! Bool)
                    {
                        addtoMaker(lati: lati, longti: longti, labelText: info[5] as! String, title: info[2] as! String, description: info[3] as! String, mapView: mapView, isDelivery: info[4] as! Bool)
                    }
                }
            }
        }
        let line = GMSPolyline(path: path)
        line.strokeWidth = 2
        line.strokeColor = .red
        line.title = "Walk"
        line.zIndex = 0
        line.map = mapView
    }
    
    func addtoMaker(lati: Double, longti: Double, labelText: String, title: String, description: String, mapView: GMSMapView, isDelivery: Bool){
        let marker = CustomMarker(labelText: labelText, isDelivery: isDelivery)
        marker.position = CLLocationCoordinate2D(latitude: CLLocationDegrees(lati), longitude: CLLocationDegrees(longti))
        marker.title = title
        marker.snippet = description
        marker.map = mapView
        path.add(CLLocationCoordinate2DMake( CLLocationDegrees(lati), CLLocationDegrees(longti)))
    }
    //defect start
    public func generateRoutes(locations: [[Any]]) {
        let location = locations[0] as? [Any]
        var origin : String = ""
        var destination : String = ""
        var waypoints: String = "waypoints=optimize:true"
        var baseURL: String = "https://maps.googleapis.com/maps/api/directions/json?"
        
        if let startLat = location?[0] as? Double, let startLong = location?[1] as? Double {
            origin = "origin=\(startLat),\(startLong)"
            destination = "destination=\(startLat),\(startLong)"
            for (index, waypoint) in locations.enumerated() {
                if(index > 0)
                {
                    waypoints += "|\(waypoint[0]),\(waypoint[1])"
                    //final destination will be the destination of the map
                    destination = "destination=\(waypoint[0]),\(waypoint[1])"
                }
            }
        }
        
        let url = "\(baseURL)\(origin)&\(destination)&\(waypoints)&key="+key
        print(url)
        if let myUrl = url.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) {
            let newUrl = URL(string: myUrl)!
            
            
            let session = URLSession.shared
            let request = NSMutableURLRequest(url: newUrl)
            request.httpMethod = "GET"
            
            
            request.addValue("application/json", forHTTPHeaderField: "Content-Type")
            request.addValue("application/json", forHTTPHeaderField: "Accept")
            
            let task = session.dataTask(with: request as URLRequest, completionHandler: { data, response, error in
                if( error != nil || data == nil) {
                    return
                }
                else{
                    guard let json = try? (JSONSerialization.jsonObject(with: data!, options: []) as! [String: Any]) else{
                        
                        return
                    }
                    
                    if let dataJSON = json["routes"] as? [[String: Any]] {
                        
                        for point in dataJSON[0]["waypoint_order"] as! [Int] {
                            self.waypoint_order_sorted.append(point)
                            let i = point + 1
                            self.sorted_location.append([self.locations[i][0], self.locations[i][1], self.locations[i][2], self.locations[i][3], self.locations[i][4], self.locations[i][5], self.locations[i][6]])
                            print([self.locations[i][0], self.locations[i][1], self.locations[i][2], self.locations[i][3], self.locations[i][4], self.locations[i][5], self.locations[i][6]])
                            
                        }
                        DispatchQueue.main.async {
                            self.generateMap()
                        }
                    }
                }
                
            })
            task.resume()
        }
    }
}

class CustomMarker: GMSMarker {
    
    var label: UILabel!
    var image: UIImageView!
    
    init(labelText: String, isDelivery: Bool) {
        
        super.init()
        
        
        var iconView = UIView(frame: CGRect(origin: .zero, size: CGSize(width: 50, height: 50)))
        label = UILabel(frame: CGRect(origin: .zero, size: CGSize(width: 50, height: 50)))
        if (labelText != "start")
        {
            
            label.textAlignment = NSTextAlignment.center
            label.numberOfLines = 0
            label.adjustsFontSizeToFitWidth = true
            label.minimumScaleFactor = 0.5
            let strokeTextAttributes = [
                NSAttributedString.Key.strokeColor : UIColor.black,
                NSAttributedString.Key.foregroundColor : UIColor.black,
                NSAttributedString.Key.strokeWidth : -5.0]
                as [NSAttributedString.Key : Any]
            
            label.attributedText = NSMutableAttributedString(string: labelText, attributes: strokeTextAttributes)
            label.font = UIFont.boldSystemFont(ofSize: 10)
            if(!isDelivery)
            {
                label.backgroundColor = UIColor(patternImage: UIImage(named: "ping_blue")!)
            }
            else{
                label.backgroundColor = UIColor(patternImage: UIImage(named: "ping")!)
            }
            
            label.textColor = UIColor.blue
            iconView.addSubview(label)
            self.iconView = iconView
        }
        else{
            iconView = UIView(frame: CGRect(origin: .zero, size: CGSize(width: 36, height: 36)))
            label = UILabel(frame: CGRect(origin: .zero, size: CGSize(width: 36, height: 36)))
            label.text = ""
            label.textAlignment = NSTextAlignment.center
            label.backgroundColor = UIColor(patternImage: UIImage(named: "current")!)
            label.textColor = UIColor.black
            
        }
        iconView.addSubview(label)
        self.iconView = iconView
    }
}
